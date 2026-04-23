package com.example.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example")
@MapperScan("com.example.nineuniverse.repository")
@EnableScheduling
public class NineUniverseApplication {

	public static void main(String[] args) {
		SpringApplication.run(NineUniverseApplication.class, args);
	}

}
