package com.wuliji.seckill.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wuliji.seckill.dao.SeckillUserDao;
import com.wuliji.seckill.domain.SeckillUser;
import com.wuliji.seckill.exception.GlobalException;
import com.wuliji.seckill.redis.RedisService;
import com.wuliji.seckill.redis.SeckillUserKey;
import com.wuliji.seckill.result.CodeMsg;
import com.wuliji.seckill.utils.MD5Utils;
import com.wuliji.seckill.utils.UUIDUtils;
import com.wuliji.seckill.vo.LoginVo;

@Service
public class SeckillUserService {
	
	public static final String COOKIE_NAME_TOKEN = "token";
	
	@Autowired
	private SeckillUserDao seckillUserDao;
	
	@Autowired
	private RedisService redisService;
	
	public SeckillUser getById(long id) {
		//取缓存
		SeckillUser user = redisService.get(SeckillUserKey.getById, ""+id, SeckillUser.class);
		if(user != null) {
			return user;
		}
		//查数据库
		user = seckillUserDao.getById(id);
		if(user != null) {
			redisService.set(SeckillUserKey.getById, ""+id, user);
		}
		return user;
	}
	
	public boolean updatePassword(String token, long id, String passwordNew) {
		//取User
		SeckillUser user = getById(id);
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//更新数据库
		SeckillUser toUpdate = new SeckillUser();
		toUpdate.setId(id);
		toUpdate.setPassword(MD5Utils.formPassToDBPass(passwordNew, user.getSalt()));
		seckillUserDao.update(toUpdate);
		//处理缓存 删除数据库对象缓存
		redisService.delete(SeckillUserKey.getById, ""+id);
		//更新缓存token
		user.setPassword(toUpdate.getPassword());
		redisService.set(SeckillUserKey.token, token,  user);
		return true;
		
	}
	
	public SeckillUser getByToken(HttpServletResponse response, String token) {
		if(StringUtils.isBlank(token)) {
			return null;
		}
		SeckillUser user = redisService.get(SeckillUserKey.token, token, SeckillUser.class);
		//延长有效期
		if(user != null) {
			addCookie(response, token, user);
		}
		return user;

	}

	public boolean login(HttpServletResponse response, LoginVo loginVo) {
		if(loginVo == null) {
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		String mobile = loginVo.getMobile();
		String formPassword = loginVo.getPassword();
		//判断手机号是否存在
		SeckillUser user = getById(Long.parseLong(mobile));
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//验证密码
		String dbPass = user.getPassword();
		String saltDB = user.getSalt();
		String calcPass = MD5Utils.formPassToDBPass(formPassword, saltDB);
		if(!calcPass.equals(dbPass)) {
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		//生成token
		String token = UUIDUtils.uuid();
		addCookie(response, token, user);
		return true;
	}
	
	private void addCookie(HttpServletResponse response, String token, SeckillUser user) {
		redisService.set(SeckillUserKey.token, token, user);
		Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
		cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
		cookie.setPath("/");
		response.addCookie(cookie);
	}

}
