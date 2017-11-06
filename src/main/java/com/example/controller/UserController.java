package com.example.controller;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dao.UserDao;
import com.example.entity.User;

@RestController
public class UserController {
	
	@Autowired
	private UserDao userDao;

	@RequestMapping({ "/me" })
	public Map<String, String> user() {
		OAuth2Authentication authentication = (OAuth2Authentication)SecurityContextHolder.getContext().getAuthentication();
		User user = userDao.getByName(authentication.getName());
		if (null == user) {
			user = new User();
			user.setName(authentication.getName());
			user = userDao.save(user);
		}

		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("name", user.getName());
		map.put("globalId", user.getId().toString());
		map.put("avatar", "http://xx.jpg");
		return map;
	}
}
