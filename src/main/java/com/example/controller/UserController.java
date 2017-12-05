package com.example.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.ClientUser;
import com.example.entity.PlatformUser;
import com.example.service.UserService;

@RestController
public class UserController {
	
	@Autowired
	private UserService userService;

	@RequestMapping({ "/me" })
	public Map<String, String> user() {
		OAuth2Authentication authentication = (OAuth2Authentication)SecurityContextHolder.getContext().getAuthentication();
		authentication = (OAuth2Authentication)authentication.getUserAuthentication();
		Map<String,String> details = (Map)authentication.getUserAuthentication().getDetails();
		
		String clientKey = details.get("client_key");
		String platformType = details.get("platform_type");
		
		PlatformUser user = userService.getPlatformUserByPlatformTypeAndNickname(platformType, authentication.getName());
		if (null == user) {
			user = new PlatformUser();
			user.setNickname(authentication.getName());
			user.setTargetType(platformType);

			user = userService.createPlatformUser(user);
		}
		
		ClientUser clientUser = userService.getClientUserByClientKeyAndPlatformUserId(clientKey, user.getId());
		if (clientUser == null) {
			clientUser = new ClientUser();
			clientUser.setNickname(authentication.getName());

			clientUser = userService.createClientUser(user);
		}
		
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("name", clientUser.getNickname());
		return map;
	}
}
