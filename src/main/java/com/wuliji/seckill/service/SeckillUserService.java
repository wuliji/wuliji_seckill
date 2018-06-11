package com.wuliji.seckill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wuliji.seckill.dao.SeckillUserDao;
import com.wuliji.seckill.domain.SeckillUser;
import com.wuliji.seckill.exception.GlobalException;
import com.wuliji.seckill.result.CodeMsg;
import com.wuliji.seckill.utils.MD5Utils;
import com.wuliji.seckill.vo.LoginVo;

@Service
public class SeckillUserService {
	
	@Autowired
	SeckillUserDao seckillUserDao;
	
	public SeckillUser getById(long id) {
		return seckillUserDao.getById(id);
	}

	public boolean login(LoginVo loginVo) {
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
		return true;
	}
}
