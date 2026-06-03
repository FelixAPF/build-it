package com.build_it.buildit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BuilditApplication {

	public static void main(String[] args) {
		SpringApplication.run(BuilditApplication.class, args);
	}

}
