package com.codeages.uc.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeages.uc.entity.ClientUser;
import com.codeages.uc.entity.PlatformUser;
import com.codeages.uc.service.UserService;

@RestController
public class UserController {
	
	@Autowired
	private UserService userService;

	@RequestMapping({ "/me" })
	public Map<String, String> user() {
		OAuth2Authentication authentication = (OAuth2Authentication)SecurityContextHolder.getContext().getAuthentication();
		authentication = (OAuth2Authentication)authentication.getUserAuthentication();
		Map<String,String> details = (Map)authentication.getUserAuthentication().getDetails();
		
		String name = authentication.getName();
		PlatformUser user = getPlatformUser(details);
		ClientUser clientUser = getClientUser(name, details, user);
		
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("name", clientUser.getNickname());
		return map;
	}

	private ClientUser getClientUser(String name, Map<String, String> details,
			PlatformUser user) {
		ClientUser clientUser = userService.getClientUserByClientKeyAndPlatformUserId(details.get("client_key"), user.getId());
		if (null == clientUser) {
			clientUser = new ClientUser();
			clientUser.setNickname(name);
			clientUser.setClientKey(details.get("client_key"));
			clientUser.setClientUserId(details.get("client_user_id"));
			clientUser.setPlatformUser(user);
			clientUser = userService.createClientUser(clientUser);
		}
		return clientUser;
	}

	private PlatformUser getPlatformUser(Map<String, String> details) {
		PlatformUser user = userService.getPlatformUserByPlatformTypeAndNickname(details.get("platformType"), details.get("name"));
		if (null == user) {
			user = userService.createPlatformUser(details);
		}
		return user;
	}
}
