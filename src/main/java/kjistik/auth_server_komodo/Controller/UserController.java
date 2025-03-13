package kjistik.auth_server_komodo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import kjistik.auth_server_komodo.Services.User.AuthService;
import kjistik.auth_server_komodo.Services.User.UserService;
import kjistik.auth_server_komodo.Utils.RequestEntities.EmailChange;
import kjistik.auth_server_komodo.Utils.RequestEntities.LoginRequest;
import kjistik.auth_server_komodo.Utils.RequestEntities.NameChange;
import kjistik.auth_server_komodo.Utils.RequestEntities.NewUser;
import kjistik.auth_server_komodo.Utils.RequestEntities.PasswordChange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class UserController {
    @Autowired
    UserService service;

    @Autowired
    AuthService authService;

   @PostMapping("/login")
public Mono<Void> logIn(@RequestBody LoginRequest login, ServerWebExchange exchange) {
    return authService.login(login, exchange)
            .onErrorResume(e -> {
                // Clear session cookie on error
                ResponseCookie invalidCookie = ResponseCookie.from("SESSION_ID", "")
                        .maxAge(0)
                        .build();
                exchange.getResponse().addCookie(invalidCookie);
                return Mono.error(e);
            });
}

    @PostMapping("/register")
    public Mono<Void> createUser(@RequestBody NewUser newUser) {
        return service.createUser(newUser);
    }

    @GetMapping("/verify")
    public Mono<Boolean> verifyUser(@RequestParam String token) {
        return service.verifyUser(token);
    }

    @PatchMapping("/api/user/updatePassword")
    public Mono<Void> updatePassword(@RequestBody PasswordChange password, @AuthenticationPrincipal UserDetails user) {
        return service.updatePassword(password.getPassword(), user.getUsername());
    }

    @PatchMapping("/api/user/updateEmail")
    public Mono<Void> updateEmail(@RequestBody EmailChange email, @AuthenticationPrincipal UserDetails user) {
        return service.updateEmail(email.getEmail(), user.getUsername());
    }

    @PatchMapping("/api/support/updateName")
    public Mono<Void> updateName(@RequestBody NameChange name) {
        return service.updateName(name.getGivenName(), name.getLastName(), name.getUsername());
    }
}