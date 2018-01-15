package com.codeages.uc.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codeages.uc.dao.BasePlatformUserDao;
import com.codeages.uc.dao.ClientUserDao;
import com.codeages.uc.dao.PlatformUserDao;
import com.codeages.uc.entity.ClientUser;
import com.codeages.uc.entity.PlatformUser;
import com.codeages.uc.service.UserService;

@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	private PlatformUserDao plateformUserDao;
	
	@Autowired
	private ClientUserDao clientUserDao;
	
	@Autowired
	private Map<String, BasePlatformUserDao> platformUserDaos;

	@Override
	public PlatformUser getPlatformUserByPlatformTypeAndNickname(String platformType, String name) {
		return plateformUserDao.getByTargetTypeAndNickname(platformType, name);
	}

	@Override
	public PlatformUser createPlatformUser(Map<String, String> details) {
		
		PlatformUser user = new PlatformUser();
		user.setNickname(details.get("nickname").toString());
		user.setTargetType(details.get("platform").toString());
		
		BasePlatformUserDao dao = platformUserDaos.get(user.getTargetType());
//		user = dao.save(user);
		return plateformUserDao.save(user);
	}

	@Override
	public ClientUser getClientUserByClientKeyAndPlatformUserId(String clientKey, Long id) {
		return clientUserDao.getByClientKeyAndPlatformUserId(clientKey, id);
	}

	@Override
	public ClientUser createClientUser(ClientUser user) {
		return clientUserDao.save(user);
	}

}
