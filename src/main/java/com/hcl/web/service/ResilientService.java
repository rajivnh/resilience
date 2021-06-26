package com.hcl.web.service;

import java.util.concurrent.CompletableFuture;

public interface ResilientService {
	public String failure();
	
	public CompletableFuture<String> timeout();
	
	public CompletableFuture<String> bulkhead();
}
