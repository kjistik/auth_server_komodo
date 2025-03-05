package kjistik.auth_server_komodo.Exceptions;

public class UserNotVerifiedException extends RuntimeException {
    public UserNotVerifiedException(String username) {
        super("User " + username + " is not verified. A verification email has been sent to your registered email");
    }

    public UserNotVerifiedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}