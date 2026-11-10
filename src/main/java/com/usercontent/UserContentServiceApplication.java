package com.usercontent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class UserContentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserContentServiceApplication.class, args);
	}

}
