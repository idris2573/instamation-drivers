package com.instamation.drivers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InstamationDriversApplication {

	public static void main(String[] args) {
		SpringApplication.run(InstamationDriversApplication.class, args);
	}
}
