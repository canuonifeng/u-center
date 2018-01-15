package com.codeages.uc.dao;

import org.springframework.stereotype.Repository;

import com.codeages.uc.entity.PlatformUser;

@Repository("wechatUserDao")
public interface WechatUserDao extends BasePlatformUserDao<PlatformUser>{


}
