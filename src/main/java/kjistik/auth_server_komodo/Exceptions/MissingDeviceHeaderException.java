package kjistik.auth_server_komodo.Exceptions;

public class MissingDeviceHeaderException extends RuntimeException {
    private final String missingHeader;

    public MissingDeviceHeaderException(String missingHeader) {
        super("Missing required device header: " + missingHeader);
        this.missingHeader = missingHeader;
    }

    public String getMissingHeader() {
        return missingHeader;
    }
}