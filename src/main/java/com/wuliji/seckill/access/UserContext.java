package com.wuliji.seckill.access;

import com.wuliji.seckill.domain.SeckillUser;

public class UserContext {
	
	private static ThreadLocal<SeckillUser> userHolder = new ThreadLocal<SeckillUser>();
	
	public static void setUser(SeckillUser user) {
		userHolder.set(user);
	}
	
	public static SeckillUser getUser() {
		return userHolder.get();
	}
}
