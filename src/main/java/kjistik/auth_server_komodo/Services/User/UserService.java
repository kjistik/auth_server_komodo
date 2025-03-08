package kjistik.auth_server_komodo.Services.User;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kjistik.auth_server_komodo.Exceptions.InvalidEmailException;
import kjistik.auth_server_komodo.Exceptions.InvalidPasswordException;
import kjistik.auth_server_komodo.Exceptions.InvalidUsernameException;
import kjistik.auth_server_komodo.Exceptions.RepeatedEmailException;
import kjistik.auth_server_komodo.Exceptions.RepeatedUserNameException;
import kjistik.auth_server_komodo.Exceptions.UserNotFoundException;
import kjistik.auth_server_komodo.Exceptions.UserNotVerifiedException;
import kjistik.auth_server_komodo.Repositories.UserRepository;
import kjistik.auth_server_komodo.Services.Email.EmailService;
import kjistik.auth_server_komodo.Utils.JwtUtils;
import kjistik.auth_server_komodo.Utils.DatabaseEntities.VerificationData;
import kjistik.auth_server_komodo.Utils.RequestEntities.NewUser;
import reactor.core.publisher.Mono;

@Service
public class UserService implements UserServiceInt {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository repo;
    @Autowired
    EmailService emailService;
    JwtUtils utils;

    public UserService(JwtUtils utils) {
        this.utils = utils;
    }

    @Override
    public Mono<Void> createUser(NewUser newUser) {
        // Normalize input
        String userName = newUser.getUserName().toLowerCase();
        String email = newUser.getEmail().toLowerCase();
        String givenName = newUser.getGivenName().toLowerCase();
        String lastName = newUser.getLastName().toLowerCase();
        String password = newUser.getPassword();

        // Validate username, email, and password
        return validateUsername(userName)
                .then(validateEmail(email))
                .then(validatePassword(password))
                .then(Mono.defer(() -> {
                    // Encode the password
                    String encodedPassword = passwordEncoder.encode(password);

                    // Create the user in the database
                    return repo.createUser(email, givenName, lastName, userName, encodedPassword)
                            .then(sendVerificationEmail(userName))
                            .then(); // Return Mono<Void> to indicate completion
                }));
    }

    private Mono<Void> validateUsername(String username) {
        String invalidUsernameMessage = "The username contains invalid characters. Only letters, numbers, underscores, and periods are allowed.";
        String usernameRegex = "^[a-zA-Z0-9_.]+$";

        if (!username.matches(usernameRegex)) {
            return Mono.error(new InvalidUsernameException(invalidUsernameMessage));
        }

        return repo.userExists(username)
                .flatMap(usernameExists -> {
                    if (usernameExists) {
                        return Mono.error(new RepeatedUserNameException(username));
                    }
                    return Mono.empty(); // Validation passed
                });
    }

    private Mono<Void> validateEmail(String email) {
        String invalidEmailMessage = "The email address is invalid. It must contain an '@' symbol, a domain name, and a top-level domain (e.g., example@domain.com).";
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        if (!email.matches(emailRegex)) {
            return Mono.error(new InvalidEmailException(invalidEmailMessage));
        }

        return repo.isEmailInUse(email)
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.error(new RepeatedEmailException(email));
                    }
                    return Mono.empty(); // Validation passed
                });
    }

    private Mono<Void> validatePassword(String password) {
        String invalidPasswordMessage = "Password must be at least 8 characters long, contain at least one uppercase letter, one special character, and one number. The \\\" character is not allowed.";
        String passwordRegex = "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\\\\|,.<>\\/?])(?=.*\\d)(?!.*[\"]).{8,}$";

        if (!password.matches(passwordRegex)) {
            return Mono.error(new InvalidPasswordException(invalidPasswordMessage));
        }

        return Mono.empty(); // Validation passed
    }

    @Override
    public Mono<Void> userExists(String username) {
        return repo.userExists(username.toLowerCase())
                .flatMap((exists) -> {
                    if (!exists) {
                        return Mono.error(new UserNotFoundException(username));
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> isEmailInUse(String email) {

        return repo.isEmailInUse(email.toLowerCase())
                .flatMap((used) -> {
                    if (used) {
                        return Mono.error(new RepeatedEmailException(email));
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Boolean> verifyUser(String token) {
        UUID id = utils.extractUserIdFromToken(token);
        return repo.verifyUser(id);
    }

    @Override
    public Mono<Void> isUserVerified(String username) {
        String user = username.toLowerCase();
        return userExists(user)
                .then(repo.isUserVerified(user)
                        .flatMap((exists) -> {
                            if (!exists) {
                                return sendVerificationEmail(user)
                                        .then(Mono.error(new UserNotVerifiedException(username)));
                            }
                            return Mono.empty();
                        }));
    }

    @Override
    public Mono<VerificationData> getVerificationData(String username) {
        return repo.findVerificationData(username.toLowerCase());
    }

    @Override
    public Mono<Void> sendVerificationEmail(String username) {
        username = username.toLowerCase();
        return repo.findByUserName(username) // Fetch the user after creation
                .flatMap(user -> {
                    // Send the verification email
                    return emailService.sendVerificationEmail(user.getEmail(),
                            utils.generateVerificationToken(user.getId()));
                })
                .then();
    }

    @Override
    public Mono<Void> updateEmail(String email, String username) {
        email = email.toLowerCase();
        username = username.toLowerCase();
        return userExists(username)
                .then(validateEmail(email))
                .then(isEmailInUse(email))
                .then(repo.updateEmail(email, username))
                .then(sendVerificationEmail(username));
    }

    @Override
    public Mono<Void> updatePassword(String password, String username) {
        return userExists(username)
                .then(validatePassword(password))
                .then(repo.updatePassword(passwordEncoder.encode(password), username));
    }

    @Override
    public Mono<Void> updateName(String givenName, String lastName, String username) {
        return userExists(username)
                .then(repo.updateName(givenName, lastName, username));
    }

}
