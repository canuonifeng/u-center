package com.example.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "user")
public class User extends BaseEntity{

	private static final long serialVersionUID = 1L;

	private String name;

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}

}
