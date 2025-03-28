package kjistik.auth_server_komodo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class AuthServerApplication {

	private static final Logger log = LoggerFactory.getLogger(AuthServerApplication.class);

	@PostConstruct
	public void initLogging() {
		Path logPath = Paths.get("logs/auth-server.log");
		try {
			Files.createDirectories(logPath.getParent());
			if (!Files.exists(logPath)) {
				Files.createFile(logPath);
			}
			log.info("Log file ready at: {}", logPath.toAbsolutePath());
		} catch (IOException e) {
			log.error("Failed to initialize logging", e);
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(AuthServerApplication.class, args);
	}

}
