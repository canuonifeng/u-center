package com.example.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "client_user")
public class ClientUser extends BaseEntity {

	private PlatformUser platformUser;
	private String clientKey;
	private String clientUserId;
	private String nickname;
	private String email;
	private String mobile;

	public PlatformUser getPlatformUser() {
		return platformUser;
	}

	public void setPlatformUser(PlatformUser platformUser) {
		this.platformUser = platformUser;
	}

	public String getClientUserId() {
		return clientUserId;
	}

	public void setClientUserId(String clientUserId) {
		this.clientUserId = clientUserId;
	}

	public String getClientKey() {
		return clientKey;
	}

	public void setClientKey(String clientKey) {
		this.clientKey = clientKey;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

}
