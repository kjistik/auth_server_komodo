package kjistik.auth_server_komodo.DTO.RequestEntities;

import java.util.List;

import lombok.Getter;

@Getter
public class RolesRequest {
    List<String> roles;
    String username;
}
