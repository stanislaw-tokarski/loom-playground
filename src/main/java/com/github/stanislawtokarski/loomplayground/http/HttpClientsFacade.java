package com.github.stanislawtokarski.loomplayground.http;

import com.github.stanislawtokarski.loomplayground.AsyncStrategy;
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

    public long sendRequests(AsyncStrategy strategy, ThreadType threads, long count) {
        log.warn("Executing {} requests using {} strategy and {} threads", count, strategy, threads);
        return httpClientPerThreadType.get(threads).execute(strategy, count);
    }

    private static final class LoomHttpClient extends ExecutorBasedHttpClient {

        LoomHttpClient(String getServerUrl) throws URISyntaxException {
            //            super(Executors.newVirtualThreadPerTaskExecutor(), getServerUrl);
            super(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory()), getServerUrl);
        }
    }

    private static final class LoomAgnosticHttpClient extends ExecutorBasedHttpClient {

        LoomAgnosticHttpClient(String getServerUrl) throws URISyntaxException {
            //            super(Executors.newFixedThreadPool(256), getServerUrl);
            super(Executors.newThreadPerTaskExecutor(Thread.ofPlatform().factory()), getServerUrl);
        }
    }
}
