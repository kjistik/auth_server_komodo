package kjistik.auth_server_komodo.Utils.DatabaseEntities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter

public class RefreshTokenValue {
    private final String token;
    private final String fingerprint;

    @JsonCreator
    public RefreshTokenValue(
            @JsonProperty("token") String token,
            @JsonProperty("fingerprint") String fingerprint) {
        this.token = token;
        this.fingerprint = fingerprint;
    }

}