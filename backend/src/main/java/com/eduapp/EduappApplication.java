package com.eduapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EduappApplication {

	public static void main(String[] args) {
		SpringApplication.run(EduappApplication.class, args);
	}

}
