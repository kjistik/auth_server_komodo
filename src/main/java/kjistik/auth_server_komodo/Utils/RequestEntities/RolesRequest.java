package kjistik.auth_server_komodo.Utils.RequestEntities;

import java.util.List;

import lombok.Getter;

@Getter
public class RolesRequest {
    List<String> roles;
    String username;
}
