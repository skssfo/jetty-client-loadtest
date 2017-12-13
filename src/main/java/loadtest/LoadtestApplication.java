package loadtest;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Security;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

@SpringBootApplication
public class LoadtestApplication {

	@Inject
	private Config config;

	@Inject
	private LoadTester loadTester;

	public LoadtestApplication() {
	}

	@PostConstruct
	public void startLoadTest() {
		System.out.println("\n\n\n\n\n ** LOAD TEST ** \n\n\n\n\n");
		System.out.println("target is " + config.getProxyEndPoint());
		System.out.println("route host is " + config.getRouteHost());

		loadTester.start();
//		HttpClient client = null;
//		try {
//			SslContextFactory sslcf = new SslContextFactory(true);
//			sslcf.setProvider("Conscrypt");
//			client = new HttpClient(sslcf);
//			client.start();
//			Request request = client.newRequest(config.getProxyEndPoint());
//			request.getHeaders().add("perf-target", config.getRouteHost());
//
//			ContentResponse cr = request.send();
//			System.out.println(cr.getStatus());
//			System.out.println(cr.getContentAsString());
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (client != null) {
//				try {
//					client.stop();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
	}

	public static void main(String[] args) {
		Security.addProvider(new org.conscrypt.OpenSSLProvider());
		SpringApplication.run(LoadtestApplication.class, args);
	}
}
