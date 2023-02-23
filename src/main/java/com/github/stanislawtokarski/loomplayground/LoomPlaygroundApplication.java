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
        var requestsExecutor = new HttpClientsFacade(dummyServiceUrl);
        var asyncPlatformTime = requestsExecutor.sendRequests(RequestsSendingStrategy.ASYNC, ThreadType.PLATFORM, 1000);
        var asyncVirtualTime = requestsExecutor.sendRequests(RequestsSendingStrategy.ASYNC, ThreadType.VIRTUAL, 1000);
        var syncPlatformTime = requestsExecutor.sendRequests(RequestsSendingStrategy.SYNC, ThreadType.PLATFORM, 1000);
        var syncVirtualTime = requestsExecutor.sendRequests(RequestsSendingStrategy.SYNC, ThreadType.VIRTUAL, 1000);
        log.warn("Took {} ms with async platform threads and {} ms with async virtual threads",
                asyncPlatformTime, asyncVirtualTime);
        log.warn("Took {} ms with sync platform threads and {} ms with sync virtual threads",
                syncPlatformTime, syncVirtualTime);
        ctx.close();
    }
}
