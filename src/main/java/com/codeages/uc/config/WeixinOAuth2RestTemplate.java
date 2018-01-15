package com.codeages.uc.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;

public class WeixinOAuth2RestTemplate extends OAuth2RestTemplate {
	public WeixinOAuth2RestTemplate(AuthorizationCodeResourceDetails resource, OAuth2ClientContext context) {
		super(resource, context);
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		messageConverters.add(new MappingJackson2HttpMessageConverter() {
			@Override
			protected boolean canRead(MediaType mediaType) {
				return true;
			}
		});
		this.setMessageConverters(messageConverters);
		this.setAccessTokenProvider(new WeixinAuthorizationCodeAccessTokenProvider(messageConverters));
	}
}