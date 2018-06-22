package com.wuliji.seckill.config;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.wuliji.seckill.access.UserContext;
import com.wuliji.seckill.domain.SeckillUser;

@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver{

	
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> clazz = parameter.getParameterType();
		return clazz == SeckillUser.class;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		//从ThreadLocal中取user信息，供方法解析
		return UserContext.getUser();
	}
	
}
