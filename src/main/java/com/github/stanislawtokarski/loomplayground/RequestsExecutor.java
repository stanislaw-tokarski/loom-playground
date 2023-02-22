package com.github.stanislawtokarski.loomplayground;

import com.github.stanislawtokarski.loomplayground.http.LoomAgnosticHttpClient;
import com.github.stanislawtokarski.loomplayground.http.LoomHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class RequestsExecutor {

    private static final Logger log = LoggerFactory.getLogger(RequestsExecutor.class);
    private static final long LOOM_AGNOSTIC_REQUESTS_COUNT = 500;
    private static final long LOOM_REQUESTS_COUNT = 500;

    private final String dummyServerAddress;

    @Autowired
    public RequestsExecutor(@Value("${api.url.service.dummy}") String dummyServerAddress) {
        this.dummyServerAddress = dummyServerAddress;
    }

    @PostConstruct
    public void scheduleRequests() throws URISyntaxException {
        var uri = new URI(dummyServerAddress + "/get");
        scheduleNonLoomRequests(uri);
        scheduleLoomRequests(uri);
    }

    private void scheduleNonLoomRequests(URI uri) {
        var loomAgnosticClient = new LoomAgnosticHttpClient(uri);
        var timeWaiting = loomAgnosticClient.execute(LOOM_AGNOSTIC_REQUESTS_COUNT);
        log.info("Spent {} ms waiting for {} requests execution WITHOUT Loom",
                timeWaiting, LOOM_AGNOSTIC_REQUESTS_COUNT);
    }

    private void scheduleLoomRequests(URI uri) {
        var loomClient = new LoomHttpClient(uri);
        var timeWaiting = loomClient.execute(LOOM_REQUESTS_COUNT);
        log.info("Spent {} ms waiting for {} requests execution WITH Loom",
                timeWaiting, LOOM_REQUESTS_COUNT);
    }
}
