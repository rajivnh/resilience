package com.hcl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.retry.annotation.Retry;

@RestController
@SpringBootApplication
public class ResilienceApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ResilienceApplication.class, args);
	}
	
	@Retry(name="backendA")
	@GetMapping("/fail")
	public String fail() {
		System.out.println("************************************************");
		
		throw new java.lang.NumberFormatException();
	}
}
