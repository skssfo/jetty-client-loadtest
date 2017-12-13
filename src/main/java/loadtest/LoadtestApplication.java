package loadtest;

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
	}

	public static void main(String[] args) {
		Security.addProvider(new org.conscrypt.OpenSSLProvider());
		SpringApplication.run(LoadtestApplication.class, args);
	}
}
