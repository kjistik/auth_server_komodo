package kjistik.auth_server_komodo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kjistik.auth_server_komodo.Services.User_role.UserRoleService;
import kjistik.auth_server_komodo.Utils.RequestEntities.RolesRequest;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth/api/roles")
public class UserRoleController {

    @Autowired
    UserRoleService service;

    @PatchMapping("/assignRole")
    Mono<Void> assignRoles(@RequestBody RolesRequest roles, @AuthenticationPrincipal UserDetails user) {
        return service.assignRole(roles.getUsername(), roles.getRoles(), user);
    }
}
