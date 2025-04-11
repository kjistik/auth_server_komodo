package kjistik.auth_server_komodo.Exceptions;

public class MissingSessionCookieException extends RuntimeException {
    public MissingSessionCookieException() {
        super("Session cookie required but not provided");
    }
}