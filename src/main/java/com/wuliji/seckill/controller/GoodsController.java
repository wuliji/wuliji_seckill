package com.wuliji.seckill.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import com.wuliji.seckill.domain.SeckillUser;
import com.wuliji.seckill.redis.GoodsKey;
import com.wuliji.seckill.redis.RedisService;
import com.wuliji.seckill.service.GoodsService;
import com.wuliji.seckill.service.SeckillUserService;
import com.wuliji.seckill.vo.GoodsVo;

@Controller
@RequestMapping("/goods")
public class GoodsController {
	
	@Autowired
	private SeckillUserService userService;
	
	@Autowired
	private RedisService redisService;
	
	@Autowired
	private GoodsService goodsService;
	
	@Autowired
	private ThymeleafViewResolver thymeleafViewResolver;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@RequestMapping(value="/to_list",produces="text/html")
	/**
	 * QPS:1200 load 15
	 * 页面静态化与对象静态话后
	 * QPS:2880 load 5
	 * @param model
	 * @param user
	 * @return
	 */
	@ResponseBody
	public String toList(HttpServletRequest request, HttpServletResponse response, Model model, SeckillUser user) {
		model.addAttribute("user", user);
		//取静态资源缓存
		String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
		if(!StringUtils.isEmpty(html)) {
			return html;
		}
		//查询商品列表
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList", goodsList);
		//return "goods_list";
		SpringWebContext ctx = new SpringWebContext(request, response, 
				request.getServletContext(), request.getLocale(), model.asMap(), applicationContext);
		//手动渲染
		html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
		if(!StringUtils.isEmpty(html)) {
			redisService.set(GoodsKey.getGoodsList, "", html);
		}
		return html;
	}
	
	@RequestMapping(value="/to_detail/{goodsId}",produces="text/html")
	@ResponseBody
	public String detail(HttpServletRequest request, HttpServletResponse response, Model model, SeckillUser user,
			@PathVariable("goodsId") long goodsId) {
		model.addAttribute("user", user);
		//取静态资源缓存
		String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
		if(!StringUtils.isEmpty(html)) {
			return html;
		}
		
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
		//return "goods_detail";
		
		SpringWebContext ctx = new SpringWebContext(request, response, 
				request.getServletContext(), request.getLocale(), model.asMap(), applicationContext);
		//手动渲染
		html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
		if(!StringUtils.isEmpty(html)) {
			redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
		}
		return html;
	}

}
