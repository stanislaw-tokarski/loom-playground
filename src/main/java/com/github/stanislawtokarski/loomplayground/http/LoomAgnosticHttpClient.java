package com.github.stanislawtokarski.loomplayground.http;

import java.net.URI;
import java.util.concurrent.Executors;

public class LoomAgnosticHttpClient extends ThreadPoolBasedHttpClient {

    public LoomAgnosticHttpClient(URI getServerURI) {
        super(Executors.newFixedThreadPool(50), getServerURI);
    }
}
