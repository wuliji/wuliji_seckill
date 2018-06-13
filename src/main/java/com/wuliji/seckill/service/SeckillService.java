package com.wuliji.seckill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wuliji.seckill.domain.OrderInfo;
import com.wuliji.seckill.domain.SeckillUser;
import com.wuliji.seckill.vo.GoodsVo;

@Service
public class SeckillService {
	
	@Autowired
	private GoodsService goodsService;
	
	@Autowired
	private OrderService orderService;

	@Transactional
	public OrderInfo seckill(SeckillUser user, GoodsVo goods) {
		//减少库存
		goodsService.reduceStock(goods);
		//orderInfo与 order表的更新
		OrderInfo orderInfo = orderService.createOrder(user, goods);
		return orderInfo;
	}

}
