package com.wuliji.seckill.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wuliji.seckill.domain.SeckillUser;
import com.wuliji.seckill.service.GoodsService;
import com.wuliji.seckill.service.SeckillUserService;
import com.wuliji.seckill.vo.GoodsVo;

@Controller
@RequestMapping("/goods")
public class GoodsController {
	
	@Autowired
	private SeckillUserService userService;
	
	@Autowired
	private GoodsService goodsService;
	
	@RequestMapping("/to_list")
	public String toList(Model model, SeckillUser user) {
		model.addAttribute("user", user);
		//查询商品列表
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList", goodsList);
		return "goods_list";
	}

}
