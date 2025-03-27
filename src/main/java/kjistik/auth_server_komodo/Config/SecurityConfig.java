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
import kjistik.auth_server_komodo.Security.Filters.JwtAuthFilter;
import kjistik.auth_server_komodo.Security.Filters.DeviceHeadersFilter; // New import

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final DeviceHeadersFilter deviceHeadersFilter; // New dependency

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
            CustomUserDetailsService customUserDetailsService,
            PasswordEncoder passwordEncoder,
            DeviceHeadersFilter deviceHeadersFilter) { // Updated constructor
        this.jwtAuthFilter = jwtAuthFilter;
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.deviceHeadersFilter = deviceHeadersFilter;
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
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/auth/api/user/**").hasRole("USER")
                        .pathMatchers("/auth/api/admin/**").hasRole("ADMIN")
                        .pathMatchers("/auth/api/support/**", "/auth/api/roles/**").hasRole("SUPPORT")
                        .pathMatchers("/auth/api/owner/**").hasRole("OWNER")
                        .pathMatchers("/auth/issue", "/auth/login", "/auth/error", "/auth/register", "/auth/verify",
                                "/test")
                        .permitAll()
                        .anyExchange().authenticated())
                .addFilterBefore(deviceHeadersFilter, SecurityWebFiltersOrder.AUTHENTICATION) // Added before JWT filter
                .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}