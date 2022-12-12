package xyz.yison;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@EnableCaching
@SpringBootApplication
public class LadderApplication {

	public static void main(String[] args) {
		SpringApplication.run(LadderApplication.class, args);
	}

}
