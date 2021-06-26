package com.hcl.web;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcl.web.service.ResilientService;

@RestController
@RequestMapping(value = "/backendA")
public class Resilience {
	@Autowired
	private ResilientService resilientService;
	
	public Resilience() {
		
	}
	
	@GetMapping("/failure")
	public String failed() {
		return resilientService.failure();
	}
	
	@GetMapping("/timeout")
	public CompletableFuture<String> timeout() {
		return resilientService.timeout();
	}
	
	@GetMapping("/bulkhead")
	public CompletableFuture<String> bulkhead() {
		return resilientService.bulkhead();
	}
}
