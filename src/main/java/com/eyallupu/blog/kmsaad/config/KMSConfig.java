package com.eyallupu.blog.kmsaad.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.kms.AWSKMS;

/**
 * Configures the KMS factory bean AND bean
 * 
 * @see AWSKMSFactoryBean
 */
@Configuration
public class KMSConfig {

	@Bean(name = "kms")
	public AWSKMSFactoryBean kmsFactory() {
		return new AWSKMSFactoryBean();
	}

	@Bean
	public AWSKMS kms() throws Exception {
		return kmsFactory().getObject();
	}
}
