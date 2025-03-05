package kjistik.auth_server_komodo.Utils.RequestEntities;

import lombok.Data;

@Data
public class NewUser {
    String email;
    String givenName;
    String lastName;
    String userName;
    String password;

}
