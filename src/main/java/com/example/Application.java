
package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedAuthoritiesExtractor;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
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
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CompositeFilter;

@SpringBootApplication
@RestController
@EnableOAuth2Client
@EnableAuthorizationServer
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class Application extends WebSecurityConfigurerAdapter {

	@Autowired
	private OAuth2ClientContext oauth2ClientContext;

	@Autowired
	private Map<String, ClientResources> map;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.antMatcher("/**").authorizeRequests().antMatchers("/", "/login**", "/a.txt", "/webjars/**").permitAll()
				.anyRequest().authenticated().and().exceptionHandling()
				.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/")).and().logout()
				.logoutSuccessUrl("/").permitAll().and().csrf()
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
				.addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
	}

	@Configuration
	@EnableResourceServer
	protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
		@Override
		public void configure(HttpSecurity http) throws Exception {
			http.antMatcher("/me").authorizeRequests().anyRequest().authenticated();
		}
	}

	@Bean
	public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(filter);
		registration.setOrder(-100);
		return registration;
	}

	@Bean(name = "github")
	@ConfigurationProperties("github")
	public ClientResources github() {
		return new ClientResources();
	}

	@Bean(name = "facebook")
	@ConfigurationProperties("facebook")
	public ClientResources facebook() {
		return new ClientResources();
	}

	@Bean(name = "wechat")
	@ConfigurationProperties("wechat")
	public ClientResources wechat() {
		return new ClientResources();
	}

	private Filter ssoFilter() {
		CompositeFilter filter = new CompositeFilter();
		List<Filter> filters = new ArrayList<>();

		for (String key : map.keySet()) {
			filters.add(ssoFilter(key, map.get(key), "/login/" + key));
		}

		filter.setFilters(filters);
		return filter;
	}

	private Filter ssoFilter(String key, ClientResources client, String path) {
		OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(path);

		OAuth2RestTemplate template = null;
		if (key.equals("wechat")) {
			template = new WeixinOAuth2RestTemplate(client.getClient(), oauth2ClientContext);
		} else {
			template = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);
		}

		UserInfoTokenServices tokenServices;
		if (key.equals("wechat")) {
			tokenServices = new WeixinnUserInfoTokenServices(oauth2ClientContext, client);
		} else {
			tokenServices = new UserInfoTokenServices(client.getResource().getUserInfoUri(),
					client.getClient().getClientId());
		}

		filter.setRestTemplate(template);
		tokenServices.setRestTemplate(template);
		filter.setTokenServices(tokenServices);
		return filter;
	}

}

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
