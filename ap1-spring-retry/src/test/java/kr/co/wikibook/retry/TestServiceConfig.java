package kr.co.wikibook.retry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;

@Configuration
@EnableResilientMethods
public class TestServiceConfig {
	@Bean
	public RetryableNotificationService fail3Service() {
		return new UnstableNotificationService(3);
	}
}
