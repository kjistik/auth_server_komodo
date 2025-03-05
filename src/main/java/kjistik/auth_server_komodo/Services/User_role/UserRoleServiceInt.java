package kjistik.auth_server_komodo.Services.User_role;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;

import reactor.core.publisher.Mono;

public interface UserRoleServiceInt {
    public Mono<Void> assignRole(String username, List<String> roles, UserDetails user);
}
