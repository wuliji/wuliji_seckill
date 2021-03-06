package com.wuliji.seckill.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wuliji.seckill.domain.SeckillUser;
import com.wuliji.seckill.domain.User;
import com.wuliji.seckill.redis.RedisService;
import com.wuliji.seckill.redis.UserKey;
import com.wuliji.seckill.result.CodeMsg;
import com.wuliji.seckill.result.Result;
import com.wuliji.seckill.service.UserService;
import com.wuliji.seckill.vo.GoodsVo;

@RestController
@RequestMapping("/demo")
public class TestController {
	
	@Autowired
	private UserService userSerivce;
	@Autowired
	private RedisService redisService;
	
	@RequestMapping("/hello2")
	public String hello2() {
		return "hello2";
	}
	
	@RequestMapping("/success")
	public Result<String> success() {
		return Result.success("hello wuliji");
	}
	
	@RequestMapping("/error")
	public Result<String> error() {
		return Result.error(CodeMsg.SERVER_ERROR);
	}
	
	@RequestMapping("/thymeleaf")
	public String thymeleaf(Model model) {
		model.addAttribute("name", "wuliji");
		return "hello";
	}
	
	@RequestMapping("/doget")
	public Result<User> doGet() {
		User user = userSerivce.getById(1);
		return Result.success(user);
	}
	
	@RequestMapping("/dotx")
	public Result<Boolean> doTx() {
		boolean tx = userSerivce.tx();
		return Result.success(true);
	}
	
	@RequestMapping("/redisGet")
	public Result<User> redisGet() {
		User v1 = redisService.get(UserKey.getById, "" + 1, User.class);
		return Result.success(v1);
	}
	
	@RequestMapping("/redisSet")
	public Result<Boolean> redisSet() {
		User user = new User();
		user.setId(1);
		user.setName("zxb1");
		Boolean set = redisService.set(UserKey.getById, "" + 1, user);
		return Result.success(set);
	}
	
	@RequestMapping("/user")
	public Result<SeckillUser> toList(Model model, SeckillUser user) {
		//查询商品列表
		return Result.success(user);
	}
	
}
