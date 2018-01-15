package com.codeages.uc.service;

import java.util.Map;

import com.codeages.uc.entity.ClientUser;
import com.codeages.uc.entity.PlatformUser;

public interface UserService {

	PlatformUser getPlatformUserByPlatformTypeAndNickname(String platformType, String name);

	PlatformUser createPlatformUser(Map<String, String> user);

	ClientUser getClientUserByClientKeyAndPlatformUserId(String clientKey, Long id);

	ClientUser createClientUser(ClientUser user);

}
