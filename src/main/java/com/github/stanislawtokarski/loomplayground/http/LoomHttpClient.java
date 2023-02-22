package com.github.stanislawtokarski.loomplayground.http;

import java.net.URI;
import java.util.concurrent.Executors;

public class LoomHttpClient extends ThreadPoolBasedHttpClient {

    public LoomHttpClient(URI getServerURI) {
        super(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory()), getServerURI);
    }
}
