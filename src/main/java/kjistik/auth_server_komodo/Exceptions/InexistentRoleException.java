package kjistik.auth_server_komodo.Exceptions;

public class InexistentRoleException extends RuntimeException {
    public InexistentRoleException(String role) {
        super("The following is not a valid role: " + role);
    }
}