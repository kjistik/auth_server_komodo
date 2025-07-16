package kjistik.auth_server_komodo.DTO.RequestEntities;

import lombok.Getter;

@Getter
public class LoginRequest {
    String username;
    String password;
}
