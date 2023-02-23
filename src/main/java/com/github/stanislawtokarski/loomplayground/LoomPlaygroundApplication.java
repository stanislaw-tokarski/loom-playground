package com.github.stanislawtokarski.loomplayground;

import com.github.stanislawtokarski.loomplayground.http.HttpClientsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URISyntaxException;

@SpringBootApplication
public class LoomPlaygroundApplication {

    private static final Logger log = LoggerFactory.getLogger(LoomPlaygroundApplication.class);

    public static void main(String[] args) throws URISyntaxException {
        var ctx = SpringApplication.run(LoomPlaygroundApplication.class, args);
        var dummyServiceUrl = ctx.getEnvironment().getProperty("api.url.service.dummy");

        var httpClient = new HttpClientsFacade(dummyServiceUrl);

        var blockingPlatformTime = httpClient.sendRequests(AsyncStrategy.BLOCKING, ThreadType.PLATFORM, 1000);
        var blockingVirtualTime = httpClient.sendRequests(AsyncStrategy.BLOCKING, ThreadType.VIRTUAL, 1000);
        var nonBlockingPlatformTime = httpClient.sendRequests(AsyncStrategy.NON_BLOCKING, ThreadType.PLATFORM, 1000);
        var nonBlockingVirtualTime = httpClient.sendRequests(AsyncStrategy.NON_BLOCKING, ThreadType.VIRTUAL, 1000);

        log.warn("""
                Times spent on waiting for completion:
                BLOCKING IMPL WITH PLATFORM THREADS: {} ms
                BLOCKING IMPL WITH VIRTUAL THREADS: {} ms
                NON BLOCKING IMPL WITH PLATFORM THREADS: {} ms
                NON BLOCKING IMPL WITH VIRTUAL THREADS: {} ms
                """, blockingPlatformTime, blockingVirtualTime, nonBlockingPlatformTime, nonBlockingVirtualTime);

        ctx.close();
    }
}
