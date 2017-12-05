package com.example.service;

import com.example.entity.ClientUser;
import com.example.entity.PlatformUser;

public interface UserService {

	PlatformUser getPlatformUserByPlatformTypeAndNickname(String platformType, String name);

	PlatformUser createPlatformUser(PlatformUser user);

	ClientUser getClientUserByClientKeyAndPlatformUserId(String clientKey, Long id);

	ClientUser createClientUser(PlatformUser user);

}
