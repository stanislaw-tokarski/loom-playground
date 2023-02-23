package com.github.stanislawtokarski.loomplayground.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.stream.LongStream;

import static java.lang.Thread.currentThread;

class ExecutorBasedHttpClient {

    private static final Logger log = LoggerFactory.getLogger(ExecutorBasedHttpClient.class);

    private final ExecutorService executorService;
    private final HttpClient httpClient;
    private final HttpRequest httpGetRequest;

    ExecutorBasedHttpClient(ExecutorService executorService, String getServerUrl) throws URISyntaxException {
        this.executorService = executorService;
        this.httpClient = HttpClient
                .newBuilder()
                .build();
        this.httpGetRequest = HttpRequest
                .newBuilder(new URI(getServerUrl))
                .GET()
                .build();
    }

    long executeAsync(long requestsCount) {
        var startTime = System.currentTimeMillis();
        var counter = new LongAdder();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        LongStream.range(0, requestsCount)
                .forEach(__ -> futures.add(
                        CompletableFuture.supplyAsync(this::sendGet, executorService)
                                .thenAcceptAsync(markSuccess(counter), executorService)
                                .exceptionallyAsync(this::logException, executorService)));
        futures.forEach(CompletableFuture::join);
        long successfulRequestsCount = counter.sum();
        if (successfulRequestsCount != requestsCount) {
            log.warn("Some requests were unsuccessful. Wanted {}, but got {}", requestsCount, successfulRequestsCount);
        }
        var finishTime = System.currentTimeMillis();
        return finishTime - startTime;
    }

    long execute(long requestsCount) {
        var startTime = System.currentTimeMillis();
        List<Future<HttpResponse<String>>> futures = new ArrayList<>();
        LongStream.range(0, requestsCount)
                .forEach(__ -> futures.add(executorService.submit(() -> {
                    try {
                        return sendGet();
                    } catch (Exception e) {
                        return null;
                    }
                })));
        var counter = new LongAdder();
        for (Future<HttpResponse<String>> future : futures) {
            try {
                var rs = future.get();
                if (rs != null) {
                    markSuccess(counter).accept(rs);
                }
            } catch (ExecutionException | InterruptedException e) {
                log.info("Computation completed exceptionally", e);
            }
        }
        long successfulRequestsCount = counter.sum();
        if (successfulRequestsCount != requestsCount) {
            log.warn("Some requests were unsuccessful. Wanted {}, but got {}", requestsCount, successfulRequestsCount);
        }
        var finishTime = System.currentTimeMillis();
        return finishTime - startTime;
    }

    private HttpResponse<String> sendGet() {
        try {
            log.debug("Thread {} [virtual={}] sending GET request",
                    currentThread().getName(), currentThread().isVirtual());
            return httpClient
                    .send(httpGetRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.info("Error when sending GET request: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Consumer<HttpResponse<String>> markSuccess(LongAdder counter) {
        return rs -> {
            if (rs.statusCode() == 200) {
                log.debug("Thread {} [virtual={}] got response with status 200",
                        currentThread().getName(), currentThread().isVirtual());
                counter.increment();
            } else {
                log.info("Thread {} [virtual={}] got response with status {}",
                        currentThread().getName(), currentThread().isVirtual(), rs.statusCode());
            }
        };
    }

    private Void logException(Throwable throwable) {
        log.info("Thread {} [virtual={}] completed exceptionally",
                currentThread().getName(), currentThread().isVirtual());
        return null;
    }
}
