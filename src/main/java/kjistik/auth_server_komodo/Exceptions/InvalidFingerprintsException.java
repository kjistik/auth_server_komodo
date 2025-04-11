package kjistik.auth_server_komodo.Exceptions;

public class InvalidFingerprintsException extends RuntimeException{
    public InvalidFingerprintsException() {
        super("The provided headers do not match with the stored device fingerprint");
    }
}
