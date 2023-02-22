package com.github.stanislawtokarski.loomplayground.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.stream.LongStream;

import static java.lang.Thread.currentThread;

public abstract class ThreadPoolBasedHttpClient {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolBasedHttpClient.class);

    private final ExecutorService executorService;
    private final HttpClient httpClient;
    private final HttpRequest getRequest;

    protected ThreadPoolBasedHttpClient(ExecutorService executorService, URI getServerURI) {
        this.executorService = executorService;
        this.httpClient = HttpClient
                .newBuilder()
                .build();
        this.getRequest = HttpRequest
                .newBuilder(getServerURI)
                .GET()
                .build();
    }

    public long execute(long requestsCount) {
        var startTime = System.currentTimeMillis();
        var counter = new LongAdder();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        LongStream.range(0, requestsCount)
                .forEach(__ -> futures.add(
                        CompletableFuture.supplyAsync(this::sendGet, executorService)
                                .thenAcceptAsync(markSuccess(counter))));
        futures.forEach(CompletableFuture::join);
        var finishTime = System.currentTimeMillis();
        long successfulRequestsCount = counter.sum();
        if (successfulRequestsCount != requestsCount) {
            log.warn("Some requests were unsuccessful. Wanted {}, but got {}", requestsCount, successfulRequestsCount);
        }
        return finishTime - startTime;
    }

    private HttpResponse<String> sendGet() {
        try {
            log.debug("Thread {} sending GET request", currentThread().getName());
            return httpClient
                    .send(getRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Consumer<HttpResponse<String>> markSuccess(LongAdder counter) {
        return rs -> {
            if (rs.statusCode() == 200) {
                log.debug("Thread {} got response with status 200", currentThread().getName());
                counter.increment();
            } else {
                log.warn("Thread {} got response with status {}", currentThread().getName(), rs.statusCode());
            }
        };
    }
}
