package hello.livsi_0820;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@OpenAPIDefinition(servers = {@Server(url = "/", description = "Default Server URL")})
public class LivSiMinseoApplication {

	public static void main(String[] args) {
		SpringApplication.run(LivSiMinseoApplication.class, args);
	}

}
