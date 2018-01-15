package com.codeages.uc.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "platform_user")
public class PlatformUser extends BaseEntity {
	
	private String nickname;
	private String targetType;
	private String targetId;
	
	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
}
