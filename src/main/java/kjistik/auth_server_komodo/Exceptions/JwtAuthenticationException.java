package kjistik.auth_server_komodo.Exceptions;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationException extends AuthenticationException {
    public JwtAuthenticationException(String msg) {
        super(msg);
    }

    public JwtAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
