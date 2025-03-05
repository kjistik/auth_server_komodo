package kjistik.auth_server_komodo.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "mailgun")
@Component
@Data
public class MailgunConfig {
    private String key;
    private String domain;
}
