package se.urvantsev.digpro;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class HttpClientConfiguration {

	@Bean
	OkHttpClient okHttpClient() {
		return new OkHttpClient.Builder().build();
	}

}
