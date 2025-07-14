package kjistik.auth_server_komodo.Services.Vault;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.ReactiveVaultOperations;

import reactor.core.publisher.Mono;

@Service
public class VaultSigningService {

    private static final Logger log = LoggerFactory.getLogger(VaultSigningService.class);

    private final ReactiveVaultOperations vaultOperations;
    private volatile PublicKey publicKey;

    @Value("${vault.transit.key-path}")
    private String keyPath;

    @Value("${vault.transit.sign-path}")
    private String signPath;

    public VaultSigningService(ReactiveVaultOperations vaultOperations) {
        this.vaultOperations = vaultOperations;
    }

    private Mono<PublicKey> initializePublicKeyReactive() {
        if (this.publicKey != null) {
            return Mono.just(this.publicKey);
        }

        return vaultOperations.read(keyPath)
            .switchIfEmpty(Mono.error(new RuntimeException("Vault returned empty response for key path: " + keyPath)))
            .flatMap(response -> {
                if (response.getData() == null) {
                    return Mono.error(new RuntimeException("Vault returned empty data for key path: " + keyPath));
                }

                Map<String, Object> data = response.getData();

                Integer latestVersion = (Integer) data.get("latest_version");
                if (latestVersion == null) {
                    return Mono.error(new RuntimeException("Vault response missing 'latest_version' for key: " + keyPath));
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> keys = (Map<String, Object>) data.get("keys");
                if (keys == null) {
                    return Mono.error(new RuntimeException("Vault response missing 'keys' object for key: " + keyPath));
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> versionData = (Map<String, Object>) keys.get(String.valueOf(latestVersion));
                if (versionData == null) {
                    return Mono.error(new RuntimeException("Vault response missing data for key version " + latestVersion + " for key: " + keyPath));
                }

                String publicKeyPem = (String) versionData.get("public_key");

                if (publicKeyPem == null || publicKeyPem.isEmpty()) {
                    return Mono.error(new RuntimeException("Vault response returned empty or null 'public_key' for: " + keyPath));
                }

                try {
                    PublicKey parsedKey = parsePemPublicKey(publicKeyPem);
                    this.publicKey = parsedKey;
                    log.info("Successfully initialized Vault public key from path {}", keyPath);
                    return Mono.just(parsedKey);
                } catch (GeneralSecurityException e) {
                    return Mono.error(new RuntimeException("Failed to parse PEM public key: " + e.getMessage(), e));
                }
            })
            .onErrorResume(e -> Mono.error(new RuntimeException("Failed to initialize Vault public key from path " + keyPath + ": " + e.getMessage(), e)))
            .cache();
    }

    @Bean("vaultPublicKey")
    public Mono<PublicKey> vaultPublicKey() {
        return initializePublicKeyReactive();
    }

    public Mono<String> sign(String signingInput) {
        return Mono.fromCallable(() -> {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(signingInput.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        })
        .flatMap(base64Hash -> {
            Map<String, String> signRequest = new HashMap<>();
            signRequest.put("input", base64Hash);
            signRequest.put("prehashed", "true");

            return vaultOperations.write(signPath, signRequest)
                    .map(response -> {
                        if (response == null || response.getData() == null) {
                            throw new RuntimeException("Vault returned empty or null signing response data.");
                        }
                        String vaultSignature = (String) response.getData().get("signature");
                        if (vaultSignature == null || vaultSignature.isEmpty()) {
                            throw new RuntimeException("Vault signing response missing 'signature'.");
                        }
                        return extractSignature(vaultSignature);
                    });
        })
        .onErrorResume(e -> Mono.error(new RuntimeException("JWT signing failed: " + e.getMessage(), e)));
    }

    private String extractSignature(String vaultSignature) {
        String[] parts = vaultSignature.split(":");
        if (parts.length < 3) {
            throw new RuntimeException("Invalid signature format from Vault. Expected at least 3 parts (e.g., 'vault:v1:signatureData'). Received: " + vaultSignature);
        }

        String base64Signature = parts[2];
        byte[] signatureBytes = Base64.getDecoder().decode(base64Signature);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
    }

    private PublicKey parsePemPublicKey(String pem) throws GeneralSecurityException {
        if (pem == null || pem.isEmpty()) {
            throw new IllegalArgumentException("PEM string cannot be null or empty.");
        }

        String publicKeyPem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(publicKeyPem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);

        return KeyFactory.getInstance("EC").generatePublic(spec);
    }
}
