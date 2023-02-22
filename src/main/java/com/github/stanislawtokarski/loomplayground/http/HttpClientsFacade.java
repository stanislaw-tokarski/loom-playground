package com.github.stanislawtokarski.loomplayground.http;

import com.github.stanislawtokarski.loomplayground.ThreadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.Executors;

public class HttpClientsFacade {

    private static final Logger log = LoggerFactory.getLogger(HttpClientsFacade.class);

    private final Map<ThreadType, ThreadPoolBasedHttpClient> httpClientPerThreadType;

    public HttpClientsFacade(String getServerUrl) throws URISyntaxException {
        this.httpClientPerThreadType = Map.of(
                ThreadType.VIRTUAL, new LoomHttpClient(getServerUrl),
                ThreadType.PLATFORM, new LoomAgnosticHttpClient(getServerUrl)
        );
    }

    public void sendRequests(ThreadType threads, long count) {
        var timeWaiting = httpClientPerThreadType.get(threads).execute(count);
        log.warn("Spent {} ms waiting for {} requests execution using {} threads", timeWaiting, count, threads);
    }

    private static final class LoomHttpClient extends ThreadPoolBasedHttpClient {

        LoomHttpClient(String getServerUrl) throws URISyntaxException {
            super(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory()), getServerUrl);
        }
    }

    private static final class LoomAgnosticHttpClient extends ThreadPoolBasedHttpClient {

        LoomAgnosticHttpClient(String getServerUrl) throws URISyntaxException {
            super(Executors.newFixedThreadPool(50), getServerUrl);
        }
    }
}
