package loadtest;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Named
public class LoadTester {

    private final static boolean DEBUG = Boolean.getBoolean("loadtest.debug");

    @Inject
    private Config config;
    private ExecutorService executor;
    private CountDownLatch latch;
    private SslContextFactory sslcf = new SslContextFactory(true);
    private HttpClient httpClient = new HttpClient(sslcf);
    private AtomicInteger errorCount = new AtomicInteger();
    //keep the latency numbers in an array, one per thread
    private long[][] latency;



    @PostConstruct
    public void init() throws Exception {
        sslcf.setProvider("Conscrypt");
        latch = new CountDownLatch(config.getThreadCount());
        latency = new long[config.getThreadCount()][config.getRequestCount()];
        httpClient.start();

        executor = Executors.newFixedThreadPool(config.getThreadCount(), r -> {
            Thread th = new Thread(r);
            th.setDaemon(true);
            return th;
        });

    }

    public void start() {
        for (int i = 0; i < config.getThreadCount(); i++) {
            executor.submit(new Task(i));
        }

        try {
            latch.await();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        printStats();

        try {
            executor.shutdown();
            httpClient.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean sendRequest() {
        try {
            Request request = httpClient.newRequest(config.getProxyEndPoint());
            request.getHeaders().add("perf-target", config.getRouteHost());

            request.method(HttpMethod.POST);
            request.content(new StringContentProvider(RandomStringUtils.randomAlphanumeric(2 * 1024)));

            ContentResponse cr = request.send();

            if (cr.getStatus() != 200) {
                return false;
            }
            //just to make sure we read the data back
            String response = cr.getContentAsString();
            if (DEBUG) {
                System.out.println(response);
            }
            response.length();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void printStats() {
        //merge all the latency numbers into a single array
        long[] allLatency = new long[config.getThreadCount() * config.getRequestCount()];
        for (int i = 0; i < config.getThreadCount(); i++) {
            for (int j = 0; j < config.getRequestCount(); j++) {
                allLatency[i * config.getRequestCount() + j] = latency[i][j];
            }
        }

        if (DEBUG) {
            for (int i = 0; i < allLatency.length; i++) {
                System.out.print(allLatency[i]);
                System.out.print(",");
            }
        }

        Arrays.sort(allLatency);

        System.out.println("\n Error count " + errorCount.get());

        printAverage(allLatency);
        System.out.println("p50 is -> " + allLatency[allLatency.length/2]);
        System.out.println("p75 is -> " + allLatency[(int) (allLatency.length * 0.75)]);
        System.out.println("p90 is -> " + allLatency[(int) (allLatency.length * 0.9)]);

    }

    private class Task implements Runnable {

        private final int threadId;

        public Task(int id) {
            this.threadId = id;
        }

        @Override
        public void run() {
            int count = 0;
            try {
                for (int i = 0; i < config.getRequestCount(); i++) {
                    long start = System.nanoTime();
                    boolean success = sendRequest();
                    long time = System.nanoTime() - start;
                    if (!success) {
                        count++;
                        latency[threadId][i] = -1;
                    } else {
                        //store in micro seconds
                        latency[threadId][i] = TimeUnit.MICROSECONDS.convert(time, TimeUnit.NANOSECONDS);
                    }
                }
            } finally {
                errorCount.addAndGet(count);
                latch.countDown();
            }

        }
    }

    private void printAverage(long[] data) {
        //ignore outliers - leave out some data in the beginning and end
        int start = (int) (data.length * 0.1);
        int end = (int) (data.length * 0.9);
        int count = 0;
        long total = 0;
        for (int i = start; i < end; i++) {
            total += data[i];
            count++;
        }

        if (count > 0) {
            System.out.println("average in msecs -> " + total / count);
        }
    }
}
