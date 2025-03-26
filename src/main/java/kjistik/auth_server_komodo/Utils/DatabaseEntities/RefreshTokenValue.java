package kjistik.auth_server_komodo.Utils.DatabaseEntities;

import lombok.Getter;

@Getter
public class RefreshTokenValue {
    public String token;
    public String fingerprint;

    public RefreshTokenValue(String token, String fingerprint) {
        this.token = token;
        this.fingerprint = fingerprint;
    }

}