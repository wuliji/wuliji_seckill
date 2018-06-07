package com.wuliji.seckill.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wuliji.seckill.result.CodeMsg;
import com.wuliji.seckill.result.Result;

@RestController
@RequestMapping("/demo")
public class HelloController {
	
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
}
