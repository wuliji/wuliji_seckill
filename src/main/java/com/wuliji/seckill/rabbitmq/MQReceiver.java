package com.wuliji.seckill.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wuliji.seckill.domain.OrderInfo;
import com.wuliji.seckill.domain.SeckillOrder;
import com.wuliji.seckill.domain.SeckillUser;
import com.wuliji.seckill.redis.RedisService;
import com.wuliji.seckill.service.GoodsService;
import com.wuliji.seckill.service.OrderService;
import com.wuliji.seckill.service.SeckillService;
import com.wuliji.seckill.vo.GoodsVo;

@Service
public class MQReceiver {

	private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
	
	@Autowired
	private SeckillService seckillService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private RedisService redisService;

	@Autowired
	private GoodsService goodsService;

	/*@RabbitListener(queues=MQConfig.QUEUE)
	public void receive(String message) {
		log.info("receive message" + message);
	}
	
	@RabbitListener(queues=MQConfig.TOPIC_QUEUE1)
	public void receiveTopic1(String message) {
		log.info("topic queue1 receive message" + message);
	}
	
	@RabbitListener(queues=MQConfig.TOPIC_QUEUE2)
	public void receiveTopic2(String message) {
		log.info("topic queue2 receive message" + message);
	}
	
	@RabbitListener(queues=MQConfig.HEADER_QUEUE)
	public void receiveHeader(byte[] message) {
		log.info("header queue receive message" + new String(message));
	}*/
	
	@RabbitListener(queues=MQConfig.SECKILL_QUEUE)
	public void receive(String message) {
		log.info("receive message" + message);
		SeckillMessage sm = RedisService.stringToBean(message, SeckillMessage.class);
		SeckillUser user = sm.getUser();
		long goodsId = sm.getGoodsId();
		
		//判断库存
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		Integer stockCount = goods.getStockCount();
		if(stockCount <= 0) {//库存不够
			return ;
		}
		//判断是否已经秒杀
		SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
		if(order != null) {
			return ;
		}
		//减库存 下订单 写入秒杀订单
		seckillService.seckill(user, goods);
	}
	
}
