package kjistik.auth_server_komodo.Exceptions;

import io.jsonwebtoken.JwtException;

public class ExpiredJWTException extends JwtException{
    public ExpiredJWTException(){
        super("Token expired beyond grace period"); 
    }
}
