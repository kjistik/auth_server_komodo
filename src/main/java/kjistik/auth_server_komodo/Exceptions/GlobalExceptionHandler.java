package kjistik.auth_server_komodo.Exceptions;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@ComponentScan
public class GlobalExceptionHandler {
    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<Object> handleJwtException(JwtAuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"" + ex.getMessage() + "\"}");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Object> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"" + ex.getMessage() + "\"}");
    }

    // Handle RepeatedUserNameException
    @ExceptionHandler(RepeatedUserNameException.class)
    public ResponseEntity<Object> handleRepeatedUserNameException(RepeatedUserNameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"error\": \"" + ex.getMessage() + "\"}");
    }

    @ExceptionHandler(RepeatedEmailException.class)
    public ResponseEntity<Object> handleRepeatedEmailException(RepeatedEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"error\": \"" + ex.getMessage() + "\"}");
    }

    @ExceptionHandler(EmailNotSentException.class)
    public ResponseEntity<Object> handleEmailNotSentException(EmailNotSentException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"" + ex.getMessage() + "\"}");
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Object> handleInvalidPasswordException(InvalidPasswordException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"error\": \"" + ex.getMessage() + "\"}");
    }

    @ExceptionHandler(InvalidUsernameException.class)
    public ResponseEntity<Object> handleInvalidUsernameException(InvalidUsernameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"error\": \"" + ex.getMessage() + "\"}");
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<Object> handleInvalidEmailException(InvalidEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"error\": \"" + ex.getMessage() + "\"}");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"" + ex.getMessage() + "\"}");
    }

    @ExceptionHandler(InexistentRoleException.class)
    public ResponseEntity<Object> handleInexistentRoleException(InexistentRoleException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"" + ex.getMessage() + "\"}");
    }

    @ExceptionHandler(UnableToAssignRoleException.class)
    public ResponseEntity<Object> handleUnableToAssignRoleException(UnableToAssignRoleException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"" + ex.getMessage() + "\"}");
    }

    @ExceptionHandler(InvalidFingerprintsException.class)
    public ResponseEntity<Object> handleInvalidFingerprintsException(InvalidFingerprintsException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("{\"error\": \"" + ex.getMessage() + "\"}");
    }

    @ExceptionHandler(ExpiredJWTException.class)
    public ResponseEntity<Object> handleExpiredJWTException(ExpiredJWTException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", "Bearer error=\"invalid_token\", error_description=\"Token expired\"")
                .body("{\"error\": \"" + ex.getMessage() + "\"}");
    }

    @ExceptionHandler(MissingSessionCookieException.class)
    public ResponseEntity<Object> handleMissingSessionCookie(MissingSessionCookieException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("Set-Cookie", "SESSION_ID=; Max-Age=0; Path=/; Secure; HttpOnly; SameSite=Strict")
                .body("""
                        {
                            "error": "Authentication required",
                            "details": "%s",
                            "code": "MISSING_SESSION_COOKIE"
                        }
                        """.formatted(ex.getMessage()));
    }
    
}
