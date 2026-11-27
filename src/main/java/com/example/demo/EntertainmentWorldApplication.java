package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.example.demo", "in.sp.main", "com.talentplatform.brand"})
@EntityScan(basePackages = {"com.example.demo", "in.sp.main", "com.talentplatform.brand"})
@EnableJpaRepositories(basePackages = {"com.example.demo", "in.sp.main", "com.talentplatform.brand"})
@EnableScheduling
public class EntertainmentWorldApplication {

	public static void main(String[] args) {
		SpringApplication.run(EntertainmentWorldApplication.class, args);
	}

}
