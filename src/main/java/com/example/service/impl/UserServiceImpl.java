package com.example.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.dao.ClientUserDao;
import com.example.dao.PlatformUserDao;
import com.example.dao.WechatUserDao;
import com.example.entity.ClientUser;
import com.example.entity.PlatformUser;
import com.example.service.UserService;

@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	private PlatformUserDao plateformDao;
	
	@Autowired
	private ClientUserDao clientUserDao;
	
	@Autowired
	private WechatUserDao wechatUserDao;

	public PlatformUser getPlatformUserByGlobalIdAndPlatformType(String globalId, String platform) {
		return null;
	}

	@Override
	public PlatformUser getPlatformUserByPlatformTypeAndNickname(String platformType, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlatformUser createPlatformUser(PlatformUser user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientUser getClientUserByClientKeyAndPlatformUserId(String clientKey, Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientUser createClientUser(PlatformUser user) {
		// TODO Auto-generated method stub
		return null;
	}

}
