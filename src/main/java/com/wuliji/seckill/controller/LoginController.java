package com.wuliji.seckill.controller;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wuliji.seckill.result.CodeMsg;
import com.wuliji.seckill.result.Result;
import com.wuliji.seckill.service.SeckillUserService;
import com.wuliji.seckill.utils.ValidatorUtil;
import com.wuliji.seckill.vo.LoginVo;

@Controller
@RequestMapping("/login")
public class LoginController {
	
	private static Logger log = LoggerFactory.getLogger(LoginController.class);
	
	@Autowired
	private SeckillUserService userService;
	
	@RequestMapping("/to_login")
	public String toLogin() {
		return "login";
	}
	
	@RequestMapping("/do_login")
	@ResponseBody
	public Result<Boolean> doLogin(@Valid LoginVo loginVo) {
		log.info(loginVo.toString());
		/*//参数校验
		String passInput = loginVo.getPassword();
		String mobile = loginVo.getMobile();
		if(StringUtils.isBlank(passInput)) {
			return Result.error(CodeMsg.PASSWORD_EMPTY);
		}
		if(!ValidatorUtil.isMobile(mobile)) {
			return Result.error(CodeMsg.PASSWORD_EMPTY);
		}*/
		//登录
		boolean ret = userService.login(loginVo);
		return Result.success(ret);
	}
	
}
