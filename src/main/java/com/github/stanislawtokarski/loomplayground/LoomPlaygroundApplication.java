package com.github.stanislawtokarski.loomplayground;

import com.github.stanislawtokarski.loomplayground.http.HttpClientsFacade;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URISyntaxException;

@SpringBootApplication
public class LoomPlaygroundApplication {

    public static void main(String[] args) throws URISyntaxException {
        var ctx = SpringApplication.run(LoomPlaygroundApplication.class, args);
        var dummyServiceUrl = ctx.getEnvironment().getProperty("api.url.service.dummy");
        var requestsExecutor = new HttpClientsFacade(dummyServiceUrl);
        requestsExecutor.sendRequests(ThreadType.PLATFORM, 100);
        requestsExecutor.sendRequests(ThreadType.VIRTUAL, 100);
        ctx.close();
    }
}
