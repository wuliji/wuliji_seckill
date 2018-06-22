package com.wuliji.seckill.access;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.HandlerMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSON;
import com.wuliji.seckill.domain.SeckillUser;
import com.wuliji.seckill.redis.AccessKey;
import com.wuliji.seckill.redis.RedisService;
import com.wuliji.seckill.result.CodeMsg;
import com.wuliji.seckill.result.Result;
import com.wuliji.seckill.service.SeckillUserService;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter{
	
	@Autowired
	private SeckillUserService userService;
	
	@Autowired
	private RedisService redisService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if(handler instanceof HandlerMethod) {//获得执行的方法
			//获取用户
			SeckillUser user = getUser(request, response);
			//根据ThreadLocal放入拦截的user信息，供方法解析用
			UserContext.setUser(user);
			
			HandlerMethod hm = (HandlerMethod) handler;
			//获取执行方法的注解
			AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
			if(accessLimit == null) {
				return true;
			}
			int seconds = accessLimit.seconds();
			int maxCount = accessLimit.maxCount();
			boolean needLogin = accessLimit.needLogin();
			String key = request.getRequestURI();
			if(needLogin) {
				if(user == null) {
					render(response, CodeMsg.SESSION_ERROR);
					return false;
				}
				key += "_" + user.getId();
			}else {
				//无需登录
			}
			//获取用户点击次数
			Integer count = redisService.get(AccessKey.withExpire(seconds), key, Integer.class);
			if(count == null) {
				redisService.set(AccessKey.withExpire(seconds), key, 1);
			}else if(count < maxCount){
				redisService.incr(AccessKey.withExpire(seconds), key);
			}else {
				render(response, CodeMsg.ACCESS_LIMIT);
				return false;
			}
			
		}
		return true;
	}
	
	/**
	 * 返回错误信息
	 * @param response 
	 * @param sESSION_ERROR
	 */
	private void render(HttpServletResponse response, CodeMsg sESSION_ERROR) throws Exception{
		response.setContentType("application/json;charset=UTF-8");
		ServletOutputStream outputStream = response.getOutputStream();
		String string = JSON.toJSONString(Result.error(sESSION_ERROR));
		outputStream.write(string.getBytes("UTF-8"));
		outputStream.flush();
		outputStream.close();
	}

	private SeckillUser getUser(HttpServletRequest request, HttpServletResponse response) {
		String paramToken = request.getParameter(SeckillUserService.COOKIE_NAME_TOKEN);
		String cookieToken = getCookieValue(request, SeckillUserService.COOKIE_NAME_TOKEN);
		
		if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(cookieToken)) {
			return null;
		}
		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		return userService.getByToken(response,token);
	}
	
	private String getCookieValue(HttpServletRequest request, String cookieNameToken) {
		Cookie[] cookies = request.getCookies();
		if(cookies == null || cookies.length <= 0) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if(cookie.getName().equals(cookieNameToken)) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
