package com.codeages.uc.config;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserApprovalRequiredException;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.MultiValueMap;

class WeixinAuthorizationCodeAccessTokenProvider extends AuthorizationCodeAccessTokenProvider {

	public WeixinAuthorizationCodeAccessTokenProvider(List<HttpMessageConverter<?>> messageConverters) {
		this.setMessageConverters(messageConverters);
		this.setTokenRequestEnhancer(new RequestEnhancer() {
			@Override
			public void enhance(AccessTokenRequest request, OAuth2ProtectedResourceDetails resource,
					MultiValueMap<String, String> form, HttpHeaders headers) {
				String clientId = form.getFirst("client_id");
				String clientSecret = form.getFirst("client_secret");
				form.set("appid", clientId);
				form.set("secret", clientSecret);
				form.remove("client_id");
				form.remove("client_secret");
			}
		});
	}

	@Override
	public OAuth2AccessToken obtainAccessToken(OAuth2ProtectedResourceDetails details, AccessTokenRequest request)
			throws UserRedirectRequiredException, UserApprovalRequiredException, AccessDeniedException,
			OAuth2AccessDeniedException {
		try {
			return super.obtainAccessToken(details, request);
		} catch (UserRedirectRequiredException e) {
			Map<String, String> params = e.getRequestParams();
			String clientId = params.get("client_id");
			params.put("appid", clientId);
			params.remove("client_id");
			throw e;
		}
	}

}