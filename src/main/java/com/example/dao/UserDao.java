package com.example.dao;

import org.springframework.stereotype.Repository;

import com.example.entity.User;

@Repository
public interface UserDao extends BaseDao<User> {

	public User getByName(String userName);
	
}
