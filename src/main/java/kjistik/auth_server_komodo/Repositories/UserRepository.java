package kjistik.auth_server_komodo.Repositories;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import kjistik.auth_server_komodo.Models.User;
import kjistik.auth_server_komodo.Utils.DatabaseEntities.VerificationData;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, UUID> {
    @Query("INSERT INTO \"user\" (email, givenname, lastname, username, password) values (:email, :givenName, :lastName, :userName, :password)")
    Mono<User> createUser(String email, String givenName, String lastName, String userName, String password);

    @Query("SELECT * FROM \"user\" WHERE username=:username")
    Mono<User> findByUserName(String username);

    @Query("SELECT EXISTS(SELECT 1 FROM \"user\" where username=:userName)")
    Mono<Boolean> userExists(String userName);

    @Query("SELECT DISTINCT \"id\", \"email\" FROM \"user\" WHERE \"username\"=:username")
    Mono<VerificationData> findVerificationData(String username);

    @Query("UPDATE \"user\" SET \"confirmed\" = true WHERE \"id\"=:id")
    Mono<Boolean> verifyUser(UUID id);

    @Query("SELECT EXISTS(SELECT 1 FROM \"user\" WHERE username=:username and confirmed=true)")
    Mono<Boolean> isUserVerified(String username);

    @Query("SELECT EXISTS(SELECT 1 from \"user\" WHERE email=:email)")
    Mono<Boolean> isEmailInUse(String email);

    @Query("UPDATE \"user\" SET email=:email, confirmed=false WHERE username=:username")
    Mono<Void> updateEmail(String email, String username);

    @Query("UPDATE \"user\" SET password=:password WHERE username=:username")
    Mono<Void> updatePassword(String password, String username);

    @Query("UPDATE \"user\" SET givenName=:givenName, lastName=:lastName WHERE username=:username")
    Mono<Void> updateName(String givenName, String lastName, String username);
}
