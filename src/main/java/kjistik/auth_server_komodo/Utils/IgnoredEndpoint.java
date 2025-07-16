package kjistik.auth_server_komodo.Utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IgnoredEndpoint {
    String path;
    String method;
}
