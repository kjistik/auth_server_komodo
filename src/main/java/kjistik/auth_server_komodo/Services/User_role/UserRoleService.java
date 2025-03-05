package kjistik.auth_server_komodo.Services.User_role;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import kjistik.auth_server_komodo.Exceptions.InexistentRoleException;
import kjistik.auth_server_komodo.Exceptions.UnableToAssignRoleException;
import kjistik.auth_server_komodo.Repositories.User_roleRepository;
import kjistik.auth_server_komodo.Services.User.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserRoleService implements UserRoleServiceInt {

    private enum RoleHierarchy {
        OWNER, // Highest level
        ADMIN, // Management level
        SUPPORT, // Support level
        USER; // Base level

        /**
         * Get the ordinal value of the role (lower ordinal = higher priority).
         */
        public int getPriority() {
            return this.ordinal();
        }

        public static List<RoleHierarchy> validateAndConvertRoles(List<String> roles) {
            return roles.stream()
                    .map(role -> {
                        try {
                            return RoleHierarchy.valueOf(role); // Convert role string to enum
                        } catch (IllegalArgumentException e) {
                            throw new InexistentRoleException(role);
                        }
                    })
                    .collect(Collectors.toList());
        }

        /**
         * Compare two roles to determine which one is higher in the hierarchy.
         */
        public static RoleHierarchy getHighestRole(Set<String> roles) {
            return roles.stream()
                    .map(RoleHierarchy::valueOf) // Convert role names to enum values
                    .min(Comparator.comparingInt(RoleHierarchy::getPriority)) // Find the role with the lowest ordinal
                    .orElseThrow(() -> new IllegalArgumentException("User has no roles"));
        }
    }

    @Autowired
    private User_roleRepository repo;

    @Autowired
    private UserService userService;

    /**
     * Get the highest role for a user.
     */
    public Mono<RoleHierarchy> getHighestRoleForUser(UserDetails user) {
        Set<String> roles = user.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", "")) // Remove "ROLE_" prefix
                .collect(Collectors.toSet());

        return Mono.just(RoleHierarchy.getHighestRole(roles)); // Return the highest role
    }

    @Override
public Mono<Void> assignRole(String username, List<String> roles, UserDetails user) {
    return userService.isUserVerified(username) // Propagates UserNotVerifiedException if not verified
        .then(getHighestRoleForUser(user))
            .flatMapMany(highestRole -> {
                // Validate and convert roles
                List<RoleHierarchy> targetRoles;
                try {
                    targetRoles = RoleHierarchy.validateAndConvertRoles(roles);
                } catch (InexistentRoleException e) {
                    return Mono.error(e); // Propagate the error
                }

                // Check if targetRoles is empty
                if (targetRoles.isEmpty()) {
                    return Mono.empty(); // No roles to assign
                }

                // Process each role reactively
                return Flux.fromIterable(targetRoles)
                    .flatMap(targetRole -> {
                        // Check if the user already has the role
                        return repo.userHasRole(username, targetRole.toString())
                            .flatMap(hasRole -> {
                                if (hasRole) {
                                    // If the user already has the role, skip and continue
                                    return Mono.empty();
                                }

                                // Check role priority (allow assigning roles with lower priority)
                                if (highestRole.getPriority() <= targetRole.getPriority()) {
                                    return repo.assignRole(username, targetRole.toString());
                                } else {
                                    return Mono.error(new UnableToAssignRoleException(targetRole.toString()));
                                }
                            });
                    });
            })
        .then(); // Return Mono<Void> after all roles are processed
}
}