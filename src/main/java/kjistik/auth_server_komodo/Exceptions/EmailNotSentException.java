package kjistik.auth_server_komodo.Exceptions;

public class EmailNotSentException extends RuntimeException {
    public EmailNotSentException(String email) {
        super("Failed to send verification email to " + email
                + "The user has been successfully created. Log in with your credentials to trigger the verification email again");
    }
}