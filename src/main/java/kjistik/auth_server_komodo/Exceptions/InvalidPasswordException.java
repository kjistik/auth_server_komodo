package kjistik.auth_server_komodo.Exceptions;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String reqirements) {
        super("The password must comply with the following requirements: " + reqirements);
    }
}