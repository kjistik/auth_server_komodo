package kjistik.auth_server_komodo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import kjistik.auth_server_komodo.Security.CustomUserDetailsService;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

        private final AuthenticationHandler authenticationHandler;
        private final JwtAuthFilter jwtAuthFilter;
        private final CustomUserDetailsService customUserDetailsService;
        private final PasswordEncoder passwordEncoder;

        public SecurityConfig(AuthenticationHandler authenticationHandler,
                        JwtAuthFilter jwtAuthFilter,
                        CustomUserDetailsService customUserDetailsService,
                        PasswordEncoder passwordEncoder) {
                this.authenticationHandler = authenticationHandler;
                this.jwtAuthFilter = jwtAuthFilter;
                this.customUserDetailsService = customUserDetailsService;
                this.passwordEncoder = passwordEncoder;
        }

        @Bean
        public ReactiveAuthenticationManager authenticationManager() {
                UserDetailsRepositoryReactiveAuthenticationManager manager = new UserDetailsRepositoryReactiveAuthenticationManager(
                                customUserDetailsService);
                manager.setPasswordEncoder(passwordEncoder);
                return manager;
        }

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
                return http
                                .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless APIs
                                .authorizeExchange(exchanges -> exchanges
                                                .pathMatchers("/api/user/**").hasRole("USER") // Restrict access to USER role
                                                .pathMatchers("/api/admin/**").hasRole("ADMIN") // Restrict access to ADMIN role
                                                .pathMatchers("/api/support/**", "/api/roles/**").hasRole("SUPPORT")
                                                .pathMatchers("/api/owner/**").hasRole("OWNER")
                                                .pathMatchers(    "/login", "/error", "/register", "/verify").permitAll() // Allow public access
                                                .anyExchange().authenticated() // Require authentication for all other endpoints
                                )
                                .formLogin(formLogin -> formLogin
                                                .loginPage("/login") // Specify the login page
                                                .authenticationSuccessHandler(authenticationHandler) // Use the reactive success handler
                                )
                                .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION) // Add JWT filter
                                .build();
        }
}