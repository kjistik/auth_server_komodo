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
}
