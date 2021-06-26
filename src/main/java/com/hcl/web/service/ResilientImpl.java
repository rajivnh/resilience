package com.hcl.web.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

@Component(value = "backendAService")
public class ResilientImpl implements ResilientService {

	public ResilientImpl() {

	}

    @Override
    @Retry(name="backendA", fallbackMethod = "fallu")
    public String failure() {
    	System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    	
        throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "This is a remote exception");
    }
    
    public String fallu(Exception e) {
    	return "Hello World!";
    }
    
    public CompletableFuture<String> gallu(Exception e) {
    	return CompletableFuture.completedFuture("from Gallu Hello World!");
    }
    
    public CompletableFuture<String> ballu(Exception e) {
    	System.out.println("@@@@@@@@@@@@@@@@@ BALLLLLLLLLLLLLLLLLLLLLLLUUUUUUUUUUUUUUUUUUU");
    	
    	return CompletableFuture.completedFuture("from Ballu Hello World!");
    }

    @Override
    @TimeLimiter(name="backendA", fallbackMethod = "gallu")
    public CompletableFuture<String> timeout() {
    	System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    	
    	return CompletableFuture.supplyAsync(() -> {
    		try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		
    		return "Hello World";
    	});
    }
    
    @Override
    @Bulkhead(name="backendB", fallbackMethod="ballu")
    public CompletableFuture<String> bulkhead() {
    	System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    	
    	return CompletableFuture.supplyAsync(() -> {
    		try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		
    		return "Hello World";
    	});
    }
}
