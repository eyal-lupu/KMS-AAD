package com.eyallupu.blog.kmsaad.config;

import org.springframework.beans.factory.FactoryBean;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;

public class KMSProvider implements FactoryBean<AWSKMS> {

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
