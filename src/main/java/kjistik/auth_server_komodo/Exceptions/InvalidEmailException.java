package kjistik.auth_server_komodo.Exceptions;

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String reqirements) {
        super("The email must comply with the following requirements: " + reqirements);
    }
}