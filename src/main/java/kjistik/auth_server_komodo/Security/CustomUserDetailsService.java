package kjistik.auth_server_komodo.Security;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kjistik.auth_server_komodo.Repositories.UserRepository;
import kjistik.auth_server_komodo.Repositories.User_roleRepository;
import reactor.core.publisher.Mono;

@Service
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final User_roleRepository user_roleRepo;

    public CustomUserDetailsService(UserRepository userRepository, User_roleRepository user_roleRepo) {
        this.userRepository = userRepository;
        this.user_roleRepo = user_roleRepo;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        System.out.println("Searching for username: " + username); // Log input
    
        return userRepository.findByUserName(username)
            .doOnNext(user -> System.out.println("User found: " + user))
            .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)))
            .flatMap(user -> 
                user_roleRepo.getUserRoles(user.getId())
                    .collectList()
                    .map(roles -> new CustomUserDetails(user, roles))
            );
    }
    
    
}
