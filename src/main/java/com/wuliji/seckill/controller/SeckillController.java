package com.wuliji.seckill.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wuliji.seckill.domain.OrderInfo;
import com.wuliji.seckill.domain.SeckillOrder;
import com.wuliji.seckill.domain.SeckillUser;
import com.wuliji.seckill.result.CodeMsg;
import com.wuliji.seckill.result.Result;
import com.wuliji.seckill.service.GoodsService;
import com.wuliji.seckill.service.OrderService;
import com.wuliji.seckill.service.SeckillService;
import com.wuliji.seckill.vo.GoodsVo;

@Controller
@RequestMapping("/seckill")
public class SeckillController {
	
	@Autowired
	private GoodsService goodsService;
	
	@Autowired
	private SeckillService seckillService;
	
	@Autowired
	private OrderService orderService;
	
	/**
	 * QPS 1300 5000并发 10次
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value="/do_seckill",method=RequestMethod.POST)
	@ResponseBody
	public Result<OrderInfo> seckill(Model model, SeckillUser user,
			@RequestParam("goodsId")long goodsId) {
		model.addAttribute("user", user);
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		//判断库存
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		Integer stockCount = goods.getStockCount();
		if(stockCount <= 0) {//库存不够
			return Result.error(CodeMsg.SECKILL_OVER);
		}
		//判断是否已经秒杀
		SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
		if(order != null) {
			return Result.error(CodeMsg.SECKILL_REPEAT);
		}
		//减库存 下订单 写入秒杀订单
		OrderInfo orderInfo = seckillService.seckill(user, goods);
		return Result.success(orderInfo);
		
	}
}
