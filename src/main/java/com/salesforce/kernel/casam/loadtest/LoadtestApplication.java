package com.salesforce.kernel.casam.loadtest;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Security;

@SpringBootApplication
public class LoadtestApplication {

	public LoadtestApplication() {
		System.out.println("\n\n\n\n\n ********************** LOAD TEST ******************* \n\n\n\n\n");
		Security.addProvider(new org.conscrypt.OpenSSLProvider());
		HttpClient client = null;
	  try {
			SslContextFactory sslcf = new SslContextFactory(true);
			sslcf.setProvider("Conscrypt");
			client = new HttpClient(sslcf);
			client.start();
			ContentResponse cr = client.GET("https://www.google.com");
			System.out.println(cr.getStatus());
			System.out.println(cr.getContentAsString());
		} catch (Exception e) {
	  	e.printStackTrace();
		} finally {
	  	if (client != null) {
	  		try {
					client.stop();
				} catch (Exception e) {
	  			e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(LoadtestApplication.class, args);
	}
}
