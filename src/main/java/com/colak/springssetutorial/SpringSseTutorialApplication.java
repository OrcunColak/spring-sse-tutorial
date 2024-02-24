package com.colak.springssetutorial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringSseTutorialApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringSseTutorialApplication.class, args);
	}

}
