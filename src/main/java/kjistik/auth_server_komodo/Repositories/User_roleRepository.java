package kjistik.auth_server_komodo.Repositories;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;

import kjistik.auth_server_komodo.Models.User_role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface User_roleRepository extends R2dbcRepository<User_role, UUID> {
    @Query("SELECT r.role AS roleName FROM role r JOIN user_role ur ON r.id = ur.role_id WHERE ur.user_id = :user_id")
    public Flux<String> getUserRoles(@Param("user_id") UUID user_id);

    @Query("insert into \"user_role\" values ((select distinct \"id\" as \"user_id\" from \"user\" where username =:username), (select distinct \"id\" as \"role_id\" from \"role\" where role=:role))")
    public Mono<Void> assignRole(String username, String role);

    @Query("SELECT EXISTS(SELECT 1 from \"user_role\" WHERE user_id=(select \"id\" from \"user\" where username=:username) AND role_id=(select \"id\" from \"role\" where role=:role))")
    public Mono<Boolean> userHasRole(String username, String role);
}
