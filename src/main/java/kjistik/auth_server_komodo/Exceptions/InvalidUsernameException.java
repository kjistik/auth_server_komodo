package kjistik.auth_server_komodo.Exceptions;

public class InvalidUsernameException extends RuntimeException {
    public InvalidUsernameException(String reqirements) {
        super("The username must comply with the following requirements: " + reqirements);
    }
}