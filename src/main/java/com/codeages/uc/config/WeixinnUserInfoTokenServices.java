package com.codeages.uc.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedAuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

class WeixinnUserInfoTokenServices extends UserInfoTokenServices {

	private ClientResources client;
	private OAuth2ClientContext context;
	private AuthoritiesExtractor authoritiesExtractor = new FixedAuthoritiesExtractor();

	public WeixinnUserInfoTokenServices(OAuth2ClientContext context, ClientResources client) {
		super(client.getResource().getUserInfoUri(), client.getClient().getClientId());
		this.client = client;
		this.context = context;
	}

	@Override
	public OAuth2Authentication loadAuthentication(String accessToken)
			throws AuthenticationException, InvalidTokenException {

		Map<String, Object> map = getMap(client.getResource().getUserInfoUri(), accessToken);

		if (map.containsKey("error")) {
			logger.debug("userinfo returned error: " + map.get("error"));
			throw new InvalidTokenException(accessToken);
		}

		map.put("name", map.get("nickname"));
		map.put("platform", "wechat");
		return extractAuthentication(map);
	}

	private OAuth2Authentication extractAuthentication(Map map) {
		Object principal = getPrincipal(map);
		List authorities = this.authoritiesExtractor.extractAuthorities(map);
		OAuth2Request request = new OAuth2Request(null, client.getClient().getClientId(), null, true, null, null, null,
				null, null);
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(principal, "N/A",
				authorities);
		token.setDetails(map);

		return new OAuth2Authentication(request, token);
	}

	private Map<String, Object> getMap(String path, String accessToken) {
		OAuth2RestOperations restTemplate = new WeixinOAuth2RestTemplate(client.getClient(), context);
		path = path + "?access_token=" + context.getAccessToken().getValue() + "&openid="
				+ context.getAccessToken().getAdditionalInformation().get("openid").toString();
		restTemplate.getOAuth2ClientContext().setAccessToken(new DefaultOAuth2AccessToken(accessToken));
		return restTemplate.getForEntity(path, Map.class).getBody();
	}
}