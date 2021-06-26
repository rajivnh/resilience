package com.hcl;

import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ZTime {

	public ZTime() {

	}
	
	// TimeLimiter
	public static void maint(String[] args) throws Exception {
		TimeLimiter timeLimiter = TimeLimiter.of(Duration.ofSeconds(3000));
		
		Callable<String> call = timeLimiter.decorateFutureSupplier(() -> CompletableFuture.supplyAsync(() -> "World"));
		
		System.out.println(call.call());
		
		System.out.println(timeLimiter.executeFutureSupplier(() -> CompletableFuture.supplyAsync(() -> {
			try {Thread.sleep(2000);}catch(Exception e) {}
			return "Hello";
		})));
		
		Thread.sleep(5000);
	}
	
	// Retry
	public static void mainr(String[] args) throws Throwable {
		RetryConfig retryConfig = RetryConfig.custom().maxAttempts(8)
				//.retryExceptions(NumberFormatException.class)
				//.ignoreExceptions(NumberFormatException.class)
				.waitDuration(Duration.ofSeconds(2)).build();         

        RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);	
        
        Retry retry = retryRegistry.retry("retryexample", retryConfig);
        
        CheckedFunction0<Integer> retryableSupplier = Retry.decorateCheckedSupplier(retry, 
        		() -> {System.out.println("RETRYING...."); return Integer.parseInt("A");});
        
        Try<Integer> result = Try.of(retryableSupplier).recover((throwable) -> 10);
        
        System.out.println(result.get());
	}
	
	// Reactive Retry
	public static void mainrr(String[] args) throws Throwable {
		RetryConfig retryConfig = RetryConfig.custom().maxAttempts(4)
				.retryExceptions(NumberFormatException.class)
				.waitDuration(Duration.ofMillis(1000)).build();         

        RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);	
        
        Retry retry = retryRegistry.retry("retryexample", retryConfig);
        
		Mono.fromCallable(() -> {	
			System.out.println("@@@@@@@@@@@@@@YYYY");
			
			Integer.parseInt("S");
			
			return Mono.error(new NumberFormatException());
		})
		.transformDeferred(RetryOperator.of(retry))
		.onErrorResume((throwable) -> {System.out.println("%%%%%%%%%%%%%%%%%%%%%% 77777"); return Mono.error(throwable);})
		.subscribe(x -> x.subscribe(System.out::println));
    
        
        Thread.sleep(38000);
	}
	
	// CircuitBreaker
	public static void mainc(String[] args) throws Throwable {
		CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
				  .failureRateThreshold(50)
				  .slowCallRateThreshold(50)
				  .waitDurationInOpenState(Duration.ofMillis(3000))
				  .slowCallDurationThreshold(Duration.ofSeconds(4))
				  .permittedNumberOfCallsInHalfOpenState(3)
				  .minimumNumberOfCalls(6)
				  .slidingWindowType(SlidingWindowType.COUNT_BASED)
				  .slidingWindowSize(5)
				  .recordExceptions(IOException.class, TimeoutException.class)
				  .build();
		
		CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
		
		CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("circuit", circuitBreakerConfig);

		circuitBreaker.getEventPublisher()
    	.onEvent(event -> {System.out.println(event.getEventType());});
    	
		CheckedFunction0<Integer> num = CircuitBreaker.decorateCheckedSupplier(circuitBreaker, () -> {
			System.out.println("******");
			Thread.sleep(3000);
			return Integer.parseInt("8");
		});

		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).isSuccess());
		
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
		System.out.println(Try.of(num).recover((throwable) -> 10).get());	
		

		System.out.println(Try.of(num).recover((throwable) -> 10).isSuccess());
		
		Thread.sleep(10000);
		
		System.out.println(Try.of(num).recover((throwable) -> 10).get());
	}
	
	public static void main77(String[] args) throws Exception {
		CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
				  .failureRateThreshold(50)
				  .slowCallRateThreshold(50)
				  .waitDurationInOpenState(Duration.ofMillis(3000))
				  .slowCallDurationThreshold(Duration.ofSeconds(14))
				  .permittedNumberOfCallsInHalfOpenState(3)
				  .minimumNumberOfCalls(6)
				  .slidingWindowType(SlidingWindowType.COUNT_BASED)
				  .slidingWindowSize(5)
				  .recordExceptions(IOException.class, TimeoutException.class)
				  .build();
		
		
		CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
		
		ReactiveResilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory = new ReactiveResilience4JCircuitBreakerFactory();
		
		reactiveResilience4JCircuitBreakerFactory.configureCircuitBreakerRegistry(circuitBreakerRegistry);
		
		ReactiveCircuitBreaker reactiveCircuitBreaker =  reactiveResilience4JCircuitBreakerFactory.create("reactive-circuit");
		
		reactiveResilience4JCircuitBreakerFactory.configureDefault(id -> {
			System.out.println("ID: " +  id);
			System.out.println("***************************************************************");
			
			CircuitBreakerConfig circuitBreakerConfig1 = circuitBreakerRegistry.find(id).isPresent()
					? circuitBreakerRegistry.find(id).get().getCircuitBreakerConfig() : circuitBreakerRegistry.getDefaultConfig();

			return new Resilience4JConfigBuilder(id).circuitBreakerConfig(circuitBreakerConfig1).build();
		});
		
		reactiveCircuitBreaker.run(Flux.range(0, 40).delayElements(Duration.ofMillis(1099)), 
				(throwable) -> Flux.error(new Error("ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!"))).subscribe(System.out::println);
		
		Thread.sleep(14000);
	}
	
	public static void main88(String[] args) throws Exception {
		CircuitBreaker circuitBreaker1 = CircuitBreaker.ofDefaults("name");
		
		Mono.fromCallable(() -> {
				Thread.sleep(13000);
				
				return "hello";
			})
			.transformDeferred(CircuitBreakerOperator.of(circuitBreaker1))
			.onErrorResume(throwable -> {System.out.println("$$$$$$$$$$$$$$$$$$$$"); return Mono.just("fallback");})
			.subscribe(System.out::println);
		
		Thread.sleep(14000);
		
		if(true)
			return;
		
		CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
				  .failureRateThreshold(50)
				  .slowCallRateThreshold(50)
				  .waitDurationInOpenState(Duration.ofMillis(3000))
				  .slowCallDurationThreshold(Duration.ofSeconds(4))
				  .permittedNumberOfCallsInHalfOpenState(3)
				  .minimumNumberOfCalls(6)
				  .slidingWindowType(SlidingWindowType.COUNT_BASED)
				  .slidingWindowSize(5)
				  .recordExceptions(IOException.class, TimeoutException.class)
				  .build();
		
		CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
		
		CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("circuit", circuitBreakerConfig);
		
		Mono.fromCallable(() -> {				
				return Flux.range(0, 40).delayElements(Duration.ofSeconds(300));
			}).transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
		.subscribe(x -> x.collectList().subscribe(System.out::println));

		Thread.sleep(14000);
	}
	
	// Reactive CircuitBreaker
	public static void mainrc(String[] args) throws Exception {
		CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
				  .failureRateThreshold(5)
				  .slowCallRateThreshold(5)
				  .waitDurationInOpenState(Duration.ofMillis(100))
				  .slowCallDurationThreshold(Duration.ofMillis(1000))
				  .permittedNumberOfCallsInHalfOpenState(3)
				  .minimumNumberOfCalls(6)
				  .slidingWindowType(SlidingWindowType.COUNT_BASED)
				  .slidingWindowSize(5)
				  .recordExceptions(IOException.class, TimeoutException.class)
				  .build();
		
		CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
		
		CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("circuit", circuitBreakerConfig);

		for(int i=0;i<10;i++) {
			System.out.println("@@@@@@@@@@@@@@ " + i);
			Thread.sleep(20);
			
			Mono.fromCallable(() -> {	
				System.out.println("@@@@@@@@@@@@@@YYYY");
				
				Thread.sleep(1000);
				return Mono.just("Hello world");
			})
			.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
			.onErrorResume((throwable) -> {System.out.println("%%%%%%%%%%%%%%%%%%%%%% 77777"); return Mono.error(throwable);})
			.subscribe(x -> x.subscribe(System.out::println));
		}
		
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%");
//		Mono<Flux<Integer>> data = Mono.fromCallable(() -> {				
//			return Flux.range(0, 40).delayElements(Duration.ofSeconds(3000));
//		}).transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
		
		//data.block().subscribe(System.out::println);
//		Flux<Integer> response = Flux.range(0, 40)
//				.delayElements(Duration.ofSeconds(4))
//				.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
//				.onErrorResume(error -> Mono.just(100));
//		
//		response.subscribe(System.out::println);
		
		Thread.sleep(64000);
	}
	
	// Bulkhead Threadpool
	public static void mainbt(String[] args) throws Exception {
		System.out.println(Runtime.getRuntime().availableProcessors());
		
		ThreadPoolBulkheadConfig config = ThreadPoolBulkheadConfig.custom()
			    .maxThreadPoolSize(8)
			    .coreThreadPoolSize(8)
			    .queueCapacity(50)
			    .build();
		
		ThreadPoolBulkhead bulkhead = ThreadPoolBulkhead.of("name", config);
		//try{Thread.sleep(1000);}catch(Exception e) {};
		for(int i=0;i<60;i++) {
			ThreadPoolBulkhead.decorateSupplier(bulkhead, () -> {					
				return CompletableFuture.supplyAsync(() -> { return "first stage";});
			}).get().thenAccept(f -> {try {
				System.out.println(f.get());
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}});
		}
		
		Thread.sleep(14000);
	}
	
	// Bulkhead
	public static void mainb(String[] args) throws Exception {
		BulkheadConfig config = BulkheadConfig.custom()
			    .maxConcurrentCalls(36)
			    .maxWaitDuration(Duration.ofMillis(500))
			    .build();

		BulkheadRegistry registry = BulkheadRegistry.of(config);
		Bulkhead bulkhead = registry.bulkhead("bulkhead");
		
		CheckedFunction0<String> function = Bulkhead.decorateCheckedSupplier(bulkhead, () -> {Thread.sleep(2000);return "Hello Corona";});
		
		ExecutorService service = Executors.newFixedThreadPool(60);
		
		Set<Callable<String>> callables = new HashSet<Callable<String>>();
		
		for(int i=0;i<40;i++) {
			callables.add(() -> {
				return Try.of(function).get();
			});
		}
		
		List<Future<String>> futures = service.invokeAll(callables);
		
		futures.forEach(f -> {
			try {
				System.out.println(f.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		});
		
		service.shutdown();
	}
	
	// Bulkhead Reactive
	public static void mainx(String[] args) throws Exception {
		BulkheadConfig config = BulkheadConfig.custom()
			    .maxConcurrentCalls(1)
			    .maxWaitDuration(Duration.ofMillis(500))
			    .build();

		BulkheadRegistry registry = BulkheadRegistry.of(config);
		Bulkhead bulkhead = registry.bulkhead("bulkhead");
		
		ExecutorService service = Executors.newFixedThreadPool(60);
		
		registry.getEventPublisher().onEntryAdded(entryAddedEvent -> {
			Bulkhead addedBulkhead = entryAddedEvent.getAddedEntry();
			
			System.out.println("Bulkhead {} added" + addedBulkhead.getName());
		}).onEntryRemoved(entryRemovedEvent -> {
			Bulkhead removedBulkhead = entryRemovedEvent.getRemovedEntry();
			
			System.out.println("Bulkhead {} removed" + removedBulkhead.getName());
		});
		
		Mono.fromCallable(() -> {return "Hello World";})
		.transformDeferred(BulkheadOperator.of(bulkhead))
		.onErrorResume(throwable -> {System.out.println(throwable.getMessage()); return Mono.error(throwable);})
		.subscribe(System.out::println);
		
		/*
		for(int i=0;i<180;i++) {
			Mono.fromCallable(() -> {return "Hello World";})
				.transformDeferred(BulkheadOperator.of(bulkhead))
				.onErrorResume(throwable -> {System.out.println(throwable.getMessage()); return Mono.error(throwable);})
				.subscribe(System.out::println);
		}
		*/
		/*
		Set<Callable<Mono<String>>> callables = new HashSet<Callable<Mono<String>>>();
		
		for(int i=0;i<80;i++) {
			callables.add(() -> {
				return Mono.fromCallable(() -> {return "Hello World";})
						.transformDeferred(BulkheadOperator.of(bulkhead));

			});
		}
		
		List<Future<Mono<String>>> futures = service.invokeAll(callables);
		
		futures.forEach(f -> {
			try {
				f.get().subscribe(System.out::println);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		});
		*/
		
		Thread.sleep(14000);
		
		service.shutdown();
	}
	
	public static void mainbu(String[] args) throws Exception {
		BulkheadConfig config = BulkheadConfig.custom()
			    .maxConcurrentCalls(1)
			    .maxWaitDuration(Duration.ofMillis(1))
			    .build();
		
		Bulkhead bulkhead = Bulkhead.of("name", config);
		//Bulkhead bulkhead = Bulkhead.ofDefaults("name");
		
		bulkhead.getEventPublisher().onEvent(entryAddedEvent -> {			
			System.out.println("Bulkhead {} added" + entryAddedEvent.getEventType());
		});
		
		ExecutorService service = Executors.newFixedThreadPool(100);
		
		Set<Callable<Mono<String>>> callables = new HashSet<Callable<Mono<String>>>();
		
		for(int i=0;i<100;i++) {
			callables.add(() -> {
				return Mono.fromCallable(() -> {return "Hello World";})
						.transformDeferred(BulkheadOperator.of(bulkhead)).delayElement(Duration.ofSeconds(1));

			});
		}
		
		List<Future<Mono<String>>> futures = service.invokeAll(callables);
		
		futures.forEach(f -> {
			try {
				f.get().delayElement(Duration.ofSeconds(1)).subscribe(System.out::println);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		});
		
		Thread.sleep(14000);
		service.shutdown();
	}
	
	public static void main(String[] args) throws Exception {
		System.setProperty("logging.level.io.nett", "off");
		System.setProperty("logging.level.root", "off");
		
		ExecutorService service = Executors.newFixedThreadPool(100);
		
		Set<Callable<Mono<String>>> callables = new HashSet<Callable<Mono<String>>>();
		
		for(int i=0;i<40;i++) {
			callables.add(new Callable<Mono<String>>() {
				@Override
				public Mono<String> call() throws Exception {
					return WebClient.create("http://127.0.0.1:8080")
						.get().uri("/backendA/bulkhead")
						.retrieve()
						.bodyToMono(String.class);
				}}
			);
		}
		
		List<Future<Mono<String>>> futures = service.invokeAll(callables);
		
		System.out.println(futures.size());
		futures.forEach(f -> {
			try {
				f.get().subscribe(System.out::println);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		});
		
		service.shutdown();
		
		Thread.sleep(14000);
	}
}
