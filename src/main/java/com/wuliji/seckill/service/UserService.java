package com.wuliji.seckill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wuliji.seckill.dao.UserDao;
import com.wuliji.seckill.domain.User;

@Service
public class UserService {
	
	@Autowired
	private UserDao userDao;
	
	public User getById(int id) {
		return userDao.getById(id);
	}
	
	@Transactional
	public boolean tx() {
		User u1 = new User();
		u1.setId(2);
		u1.setName("wuliji2");
		userDao.insert(u1);
		
		User u2 = new User();
		u1.setId(1);
		u1.setName("wuliji1");
		userDao.insert(u2);
		return true;
	}
}
