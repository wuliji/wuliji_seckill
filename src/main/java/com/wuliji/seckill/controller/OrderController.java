package com.wuliji.seckill.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wuliji.seckill.domain.OrderInfo;
import com.wuliji.seckill.domain.SeckillUser;
import com.wuliji.seckill.result.CodeMsg;
import com.wuliji.seckill.result.Result;
import com.wuliji.seckill.service.GoodsService;
import com.wuliji.seckill.service.OrderService;
import com.wuliji.seckill.vo.GoodsVo;
import com.wuliji.seckill.vo.OrderDetailVo;

@Controller
@RequestMapping("/order")
public class OrderController {
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private GoodsService goodsService;
	
	@RequestMapping("/detail")
	@ResponseBody
	public Result<OrderDetailVo> detail(Model model, SeckillUser user,
			@RequestParam("orderId")long orderId){
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		OrderInfo order = orderService.getOrderById(orderId);
		if(order == null) {
			return Result.error(CodeMsg.ORDER_NOT_EXIST);
		}
		Long id = order.getGoodsId();
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(id);
		OrderDetailVo vo = new OrderDetailVo();
		vo.setGoods(goods);
		vo.setOrder(order);
		return Result.success(vo);
	}
}
