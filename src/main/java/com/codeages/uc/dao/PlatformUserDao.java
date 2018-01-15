package com.codeages.uc.dao;

import org.springframework.stereotype.Repository;

import com.codeages.uc.entity.PlatformUser;

@Repository
public interface PlatformUserDao extends BaseDao<PlatformUser>{

	PlatformUser getByTargetTypeAndNickname(String platformType, String name);


}
