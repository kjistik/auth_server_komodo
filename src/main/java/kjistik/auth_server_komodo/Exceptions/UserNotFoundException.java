package kjistik.auth_server_komodo.Exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String username) {
        super("No user has been found with the username: " + username);
    }
}
