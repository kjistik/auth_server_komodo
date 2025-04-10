package kjistik.auth_server_komodo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
import kjistik.auth_server_komodo.Utils.RequestEntities.TokenResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class UserController {
    @Autowired
    UserService service;

    @Autowired
    AuthService authService;

    @PostMapping("/login")
    public Mono<Void> logIn(@RequestBody LoginRequest login,
            ServerWebExchange exchange,
            @RequestHeader("X-OS") String os,
            @RequestHeader("X-Timezone") String timezone,
            @RequestHeader("X-Resolution") String resolution,
            @RequestHeader("User-Agent") String agent) {
        return authService.login(login, exchange, agent, os, resolution, timezone)
                .onErrorResume(e -> {
                    // Clear session cookie on error
                    ResponseCookie invalidCookie = ResponseCookie.from("SESSION_ID", "")
                            .maxAge(0)
                            .build();
                    exchange.getResponse().addCookie(invalidCookie);
                    return Mono.error(e);
                });
    }

    @PostMapping("/reissue")
    public Mono<TokenResponse> reIssue(
            @CookieValue("SESSION_ID") String sessionId,
            @RequestHeader("X-OS") String os,
            @RequestHeader("X-Timezone") String timezone,
            @RequestHeader("X-Resolution") String resolution,
            @RequestHeader("User-Agent") String agent,
            @RequestHeader("Authorization") String authHeader) {
        String jwtToken = authHeader.substring(7);
        System.out.println("Still testing");
        return authService.reIssueToken(agent, os, resolution, timezone, sessionId, jwtToken);
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

    @DeleteMapping("/logout")
    public Mono<Boolean> endSession(@CookieValue("SESSION_ID") String sessionId,
            @AuthenticationPrincipal UserDetails user) {
        return authService.endSession(user.getUsername(), sessionId);

    }
}