package kjistik.auth_server_komodo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import kjistik.auth_server_komodo.Services.RefreshToken.RefreshTokenService;
import kjistik.auth_server_komodo.Utils.RequestEntities.UsernameRequest;
import reactor.core.publisher.Mono;

@RestController
public class TestController {

    @Autowired
    RefreshTokenService service;

    @PostMapping("/test")
    public Mono<String> getRefreshToken(@RequestBody UsernameRequest user,
            @CookieValue(name = "session_id", required = true) String session) {
        return service.getRefreshToken(user.getUsername(), session);
    }
}
