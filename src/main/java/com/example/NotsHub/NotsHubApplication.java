package com.example.NotsHub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotsHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotsHubApplication.class, args);
	}

}
