package com.codeages.uc.dao;

import org.springframework.stereotype.Repository;

import com.codeages.uc.entity.ClientUser;

@Repository
public interface ClientUserDao extends BaseDao<ClientUser>{

	ClientUser getByClientKeyAndPlatformUserId(String clientKey, Long platformUserId);
}
