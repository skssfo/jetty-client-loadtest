package loadtest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Value("${loadtest.proxy.endpoint}")
    private String proxyEndPoint;

    @Value("${loadtest.proxy.routeHost}")
    private String routeHost;

    @Value("${loadtest.thread.count}")
    private int threadCount;

    @Value("${loadtest.request.count}")
    private int requestCount;

    @Value("${loadtest.ignore.range}")
    private int ignoreRange;

    public String getProxyEndPoint() {
        return proxyEndPoint;
    }

    public String getRouteHost() {
        return routeHost;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public int getIgnoreRange() {
        return ignoreRange;
    }
}
