package kjistik.auth_server_komodo.Services.User;

import kjistik.auth_server_komodo.Utils.DatabaseEntities.VerificationData;
import kjistik.auth_server_komodo.Utils.RequestEntities.NewUser;
import reactor.core.publisher.Mono;

public interface UserServiceInt {

    public Mono<Void> createUser(NewUser newUser);

    public Mono<Void> userExists(String username);

    public Mono<Boolean> verifyUser(String token);

    Mono<Void> isUserVerified(String username);

    public Mono<Void> isEmailInUse(String email);

    public Mono<VerificationData> getVerificationData(String username);

    public Mono<Void> sendVerificationEmail(String username);

    public Mono<Void> updateEmail(String email, String username);

    public Mono<Void> updatePassword(String password, String username);

    Mono<Void> updateName(String givenName, String lastName, String username);
}
