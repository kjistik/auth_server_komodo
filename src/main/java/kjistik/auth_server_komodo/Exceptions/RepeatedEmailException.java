package kjistik.auth_server_komodo.Exceptions;

public class RepeatedEmailException extends RuntimeException {
    public RepeatedEmailException(String email) {
        super("Email " + email + " is already in use");
    }
}