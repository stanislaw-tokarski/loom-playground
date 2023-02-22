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
        var platformTime = requestsExecutor.sendRequests(ThreadType.PLATFORM, 100);
        var virtualTime = requestsExecutor.sendRequests(ThreadType.VIRTUAL, 100);
        log.info("Took {} ms with platform threads and {} ms with virtual threads", platformTime, virtualTime);
        ctx.close();
    }
}
