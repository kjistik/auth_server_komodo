package kjistik.auth_server_komodo.Services.Email;

import reactor.core.publisher.Mono;

public interface EmailServiceInt {
    public Mono<Void> sendVerificationEmail(String toMail, String verificationLink);

    public Mono<Void> sendSuspiciousActivityEmail(String userEmail, String os, String browser);
}
