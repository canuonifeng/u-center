/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserApprovalRequiredException;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CompositeFilter;

@SpringBootApplication
@RestController
@EnableOAuth2Client
@EnableAuthorizationServer
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SocialApplication extends WebSecurityConfigurerAdapter {

	@Autowired
	private OAuth2ClientContext oauth2ClientContext;
	
	@Autowired
	private Map<String, ClientResources> map;

	@RequestMapping({ "/user", "/me" })
	public Map<String, String> user(Principal principal) {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("name", principal.getName());
		return map;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http.antMatcher("/**").authorizeRequests().antMatchers("/", "/login**", "/webjars/**").permitAll().anyRequest()
				.authenticated().and().exceptionHandling()
				.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/")).and().logout()
				.logoutSuccessUrl("/").permitAll().and().csrf()
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
				.addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
		// @formatter:on
	}

	@Configuration
	@EnableResourceServer
	protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
		@Override
		public void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http.antMatcher("/me").authorizeRequests().anyRequest().authenticated();
			// @formatter:on
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(SocialApplication.class, args);
	}

	@Bean
	public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(filter);
		registration.setOrder(-100);
		return registration;
	}

	@Bean(name="github")
	@ConfigurationProperties("github")
	public ClientResources github() {
		return new ClientResources();
	}

	@Bean(name="facebook")
	@ConfigurationProperties("facebook")
	public ClientResources facebook() {
		return new ClientResources();
	}
	
	@Bean(name="wechat")
	@ConfigurationProperties("wechat")
	public ClientResources wechat() {
		return new ClientResources();
	}

	private Filter ssoFilter() {
		CompositeFilter filter = new CompositeFilter();
		List<Filter> filters = new ArrayList<>();
		
		for (String key : map.keySet()) {
			filters.add(ssoFilter(key, map.get(key), "/login/"+key));
		}
		
		filter.setFilters(filters);
		return filter;
	}

	private Filter ssoFilter(String key, ClientResources client, String path) {
		OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(
				path);
		
		OAuth2RestTemplate template = null ;
		if (key.equals("wechat")) {
			template = new WeixinOAuth2RestTemplate(client.getClient(), oauth2ClientContext);
		} else {
			template = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);
		}
		
		
		filter.setRestTemplate(template);
		
		UserInfoTokenServices tokenServices = new UserInfoTokenServices(
				client.getResource().getUserInfoUri(), client.getClient().getClientId());
		tokenServices.setRestTemplate(template);
		filter.setTokenServices(tokenServices);
		return filter;
	}

}

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
	public OAuth2AccessToken obtainAccessToken(OAuth2ProtectedResourceDetails details,
			AccessTokenRequest request) throws UserRedirectRequiredException, UserApprovalRequiredException,
			AccessDeniedException, OAuth2AccessDeniedException {
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

class WeixinOAuth2RestTemplate extends OAuth2RestTemplate {
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

	@Override
	protected URI appendQueryParameter(URI uri, OAuth2AccessToken accessToken) {
		uri = super.appendQueryParameter(uri, accessToken);
		String url = uri.toString();
		if (url.contains("$openid$")) {
			String openid = (String) accessToken.getAdditionalInformation().get("openid");
			try {
				uri = new URI(url.replace("$openid$", openid));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return uri;
	}
}

class ClientResources {

	@NestedConfigurationProperty
	private AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails();

	@NestedConfigurationProperty
	private ResourceServerProperties resource = new ResourceServerProperties();

	public AuthorizationCodeResourceDetails getClient() {
		return client;
	}

	public ResourceServerProperties getResource() {
		return resource;
	}
}
