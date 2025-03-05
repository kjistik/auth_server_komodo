package kjistik.auth_server_komodo.Exceptions;

public class UnableToAssignRoleException extends RuntimeException{
    public UnableToAssignRoleException(String role){
        super("Unable to assign the role " + role + ". You must have at least the same role to do so");
    }
}
