package com.wuliji.seckill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wuliji.seckill.domain.OrderInfo;
import com.wuliji.seckill.domain.SeckillOrder;
import com.wuliji.seckill.domain.SeckillUser;
import com.wuliji.seckill.redis.RedisService;
import com.wuliji.seckill.redis.SeckillKey;
import com.wuliji.seckill.vo.GoodsVo;

@Service
public class SeckillService {
	
	@Autowired
	private GoodsService goodsService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private RedisService redisService;

	@Transactional
	public OrderInfo seckill(SeckillUser user, GoodsVo goods) {
		//减少库存
		boolean success = goodsService.reduceStock(goods);
		if(success) {
			//orderInfo与 order表的更新
			OrderInfo orderInfo = orderService.createOrder(user, goods);
			return orderInfo;
		}else {
			setGoodsOver(goods.getId());
			return null;
		}
	}


	/**
	 * 获取秒杀结果
	 * @param id
	 * @param goodsId
	 * @return
	 */
	public long getSeckillResult(Long id, long goodsId) {
		SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(id, goodsId);
		if(order != null) {//秒杀成功
			return order.getOrderId();
		}else {
			boolean isOver = getGoodsOver(goodsId);
			if(isOver) {
				return -1;
			}else {
				return 0;
			}
		}
	}

	
	private void setGoodsOver(Long id) {
		redisService.set(SeckillKey.isGoodsOver, ""+id, true);
	}
	
	private boolean getGoodsOver(long goodsId) {
		return redisService.exists(SeckillKey.isGoodsOver, ""+goodsId);
	}

}
