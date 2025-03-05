package kjistik.auth_server_komodo.Utils.DatabaseEntities;

import java.util.UUID;

import lombok.Getter;

@Getter
public class VerificationData {
    UUID id;
    String email;
}
