package kjistik.auth_server_komodo.Exceptions;

public class RepeatedUserNameException extends RuntimeException {
    public RepeatedUserNameException(String userName) {
        super("Username " + userName + " is already in use");
    }
}