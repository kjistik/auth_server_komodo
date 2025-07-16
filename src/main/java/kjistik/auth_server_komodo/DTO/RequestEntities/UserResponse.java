package kjistik.auth_server_komodo.DTO.RequestEntities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    String username;
    String email;
    String givenName;
    String lastName;
}
