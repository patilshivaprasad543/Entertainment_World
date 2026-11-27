package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.example.demo", "in.sp.main"})
@EntityScan(basePackages = {"com.example.demo", "in.sp.main"})
@EnableJpaRepositories(basePackages = {"com.example.demo", "in.sp.main"})
public class EntertainmentWorldApplication {

	public static void main(String[] args) {
		SpringApplication.run(EntertainmentWorldApplication.class, args);
	}

}
