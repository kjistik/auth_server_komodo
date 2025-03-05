package kjistik.auth_server_komodo.Services.Email;

import org.springframework.stereotype.Service;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;

import kjistik.auth_server_komodo.Config.KomodoConfig;
import kjistik.auth_server_komodo.Config.MailgunConfig;
import kjistik.auth_server_komodo.Exceptions.EmailNotSentException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class EmailService implements EmailServiceInt {

    private final MailgunConfig config;
    private final KomodoConfig domainConfig;

    public EmailService(MailgunConfig config, KomodoConfig domainConfig) {
        this.config = config;
        this.domainConfig = domainConfig;
    }

    
    @Override
    public Mono<Void> sendVerificationEmail(String toMail, String verificationLink) {
        String url = domainConfig.getDomainUrl().concat("/verify?token=");
        return Mono.fromCallable(() -> {
            System.out.println("Ahí tiro el correo, pa\n");
            // Create the Mailgun API client
            MailgunMessagesApi mailgunMessagesApi = MailgunClient.config(config.getKey())
                    .createApi(MailgunMessagesApi.class);

            // Build the email message with HTML content
            String htmlContent = "<html>"
                    + "<body>"
                    + "<p>Por favor compruebe su correo electrónico haciendo click en el siguiente enlace:</p>"
                    + "<p><a href=\"" + url + verificationLink + "\">Verificar correo electrónico</a></p>"
                    + "<p>En caso de no haberse registrado, por favor ignore este mensaje.</p>"
                    + "</body>"
                    + "</html>";

            Message message = Message.builder()
                    .from("noreply@" + config.getDomain())
                    .to(toMail)
                    .subject("Verificación de correo electrónico")
                    .html(htmlContent) // Use HTML content instead of plain text
                    .build();

            // Send the email and get the response
            MessageResponse response = mailgunMessagesApi.sendMessage(config.getDomain(), message);

            // Check if the email was sent successfully
            if (response.getId() == null) {
                throw new EmailNotSentException(toMail); // Throw the exception directly
            }

            return response; // This value is ignored since the method returns Mono<Void>
        })
                .subscribeOn(Schedulers.boundedElastic()) // Run blocking code on a separate thread
                .then(); // Convert the Mono<MessageResponse> to Mono<Void>
    }
}