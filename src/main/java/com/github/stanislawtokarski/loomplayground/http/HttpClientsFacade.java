package com.github.stanislawtokarski.loomplayground.http;

import com.github.stanislawtokarski.loomplayground.RequestsSendingStrategy;
import com.github.stanislawtokarski.loomplayground.ThreadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.Executors;

public class HttpClientsFacade {

    private static final Logger log = LoggerFactory.getLogger(HttpClientsFacade.class);

    private final Map<ThreadType, ExecutorBasedHttpClient> httpClientPerThreadType;

    public HttpClientsFacade(String getServerUrl) throws URISyntaxException {
        this.httpClientPerThreadType = Map.of(
                ThreadType.VIRTUAL, new LoomHttpClient(getServerUrl),
                ThreadType.PLATFORM, new LoomAgnosticHttpClient(getServerUrl)
        );
    }

    public long sendRequests(RequestsSendingStrategy strategy, ThreadType threads, long count) {
        log.warn("Executing requests using {} strategy and {} threads", strategy, threads);
        var httpClient = httpClientPerThreadType.get(threads);
        return switch (strategy) {
            case ASYNC -> httpClient.executeAsync(count);
            case SYNC -> httpClient.execute(count);
        };
    }

    private static final class LoomHttpClient extends ExecutorBasedHttpClient {

        LoomHttpClient(String getServerUrl) throws URISyntaxException {
            //            super(Executors.newVirtualThreadPerTaskExecutor(), getServerUrl);
            super(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory()), getServerUrl);
        }
    }

    private static final class LoomAgnosticHttpClient extends ExecutorBasedHttpClient {

        LoomAgnosticHttpClient(String getServerUrl) throws URISyntaxException {
            //            super(Executors.newFixedThreadPool(50), getServerUrl);
            super(Executors.newThreadPerTaskExecutor(Thread.ofPlatform().factory()), getServerUrl);
        }
    }
}
