package kr.co.wikibook.healthchecker;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HealthCheckerApplication {

	static void main(String[] args) {
		SpringApplication.run(HealthCheckerApplication.class, args);
	}

	@Bean
	public JobRegistry jobRegistry() {
		return new MapJobRegistry();
	}
}
