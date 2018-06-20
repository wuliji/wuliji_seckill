package com.wuliji.seckill.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wuliji.seckill.dao.OrderDao;
import com.wuliji.seckill.domain.OrderInfo;
import com.wuliji.seckill.domain.SeckillOrder;
import com.wuliji.seckill.domain.SeckillUser;
import com.wuliji.seckill.redis.OrderKey;
import com.wuliji.seckill.redis.RedisService;
import com.wuliji.seckill.vo.GoodsVo;

@Service
public class OrderService {
	
	@Autowired
	private OrderDao orderDao;
	
	@Autowired
	private RedisService redisService;
	
	public SeckillOrder getSeckillOrderByUserIdGoodsId(long userId, long goodsId) {
		//return orderDao.getSeckillOrderByUserIdGoodsId(userId, goodsId);
		return redisService.get(OrderKey.getSeckillOrderByUidGid, ""+userId+"_"+goodsId, SeckillOrder.class);
	}
	
	@Transactional
	public OrderInfo createOrder(SeckillUser user, GoodsVo goods) {
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setCreateDate(new Date());
		orderInfo.setDeliveryAddrId(0L);
		orderInfo.setGoodsCount(1);
		orderInfo.setGoodsId(goods.getId());
		orderInfo.setGoodsName(goods.getGoodsName());
		orderInfo.setGoodsPrice(goods.getSeckillPrice());
		orderInfo.setOrderChannel(1);
		orderInfo.setStatus(0);//0新建未支付
		orderInfo.setUserId(user.getId());
		long orderId = orderDao.insertOrder(orderInfo);
		SeckillOrder seckillOrder = new SeckillOrder();
		seckillOrder.setGoodsId(goods.getId());
		seckillOrder.setOrderId(orderInfo.getId());
		seckillOrder.setUserId(user.getId());
		orderDao.insertSeckillOrder(seckillOrder);
		//将订单写入缓存中去
		redisService.set(OrderKey.getSeckillOrderByUidGid, ""+user.getId()+"_"+goods.getId(), seckillOrder);
		return orderInfo;
	}

	public OrderInfo getOrderById(long orderId) {
		return orderDao.getOrderById(orderId);
	}
	
}
