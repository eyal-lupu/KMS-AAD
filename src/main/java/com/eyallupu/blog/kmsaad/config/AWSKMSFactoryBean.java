package com.eyallupu.blog.kmsaad.config;

import org.springframework.beans.factory.FactoryBean;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;

/**
 * A simple factory bean to provide the AWSKMS instance (as a singleton). This
 * factory bean is registered using {@link KMSConfig}.
 */
public class AWSKMSFactoryBean implements FactoryBean<AWSKMS> {

	private AWSKMS awskms = AWSKMSClientBuilder.defaultClient();

	@Override
	public AWSKMS getObject() throws Exception {
		return awskms;
	}

	@Override
	public Class<?> getObjectType() {
		return AWSKMS.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
