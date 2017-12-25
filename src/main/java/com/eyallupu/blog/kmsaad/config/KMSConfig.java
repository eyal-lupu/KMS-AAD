package com.eyallupu.blog.kmsaad.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.kms.AWSKMS;

@Configuration
public class KMSConfig {

	@Bean(name = "kms")
	public KMSProvider kmsFactory() {
		return new KMSProvider();
	}

	@Bean
	public AWSKMS kms() throws Exception {
		return kmsFactory().getObject();
	}
}
