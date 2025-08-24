package hello.livsi_0820;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class LivSiMinseoApplication {

	public static void main(String[] args) {
		SpringApplication.run(LivSiMinseoApplication.class, args);
	}

}
