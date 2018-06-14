package com.wuliji.seckill.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
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
	/**
	 * QPS:1200
	 * @param model
	 * @param user
	 * @return
	 */
	public String toList(Model model, SeckillUser user) {
		model.addAttribute("user", user);
		//查询商品列表
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList", goodsList);
		return "goods_list";
	}
	
	@RequestMapping("/to_detail/{goodsId}")
	public String detail(Model model, SeckillUser user,
			@PathVariable("goodsId") long goodsId) {
		model.addAttribute("user", user);
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		model.addAttribute("goods", goods);
		
		long startAt = goods.getStartDate().getTime();
		long endAt = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();
		
		int seckillStatus = 0;//秒杀状态
		int remainSeconds = 0;//剩余多少秒
		
		if(now < startAt) {//秒杀没开始，倒计时
			seckillStatus = 0;
			remainSeconds = (int) ((startAt - now)/1000);
		}else if(now > endAt){//秒杀以结束
			seckillStatus = 2;
			remainSeconds = -1;
		}else {
			seckillStatus = 1;
			remainSeconds = 0;
		}
		
		model.addAttribute("seckillStatus", seckillStatus);
		model.addAttribute("remainSeconds", remainSeconds);
		return "goods_detail";
	}

}
