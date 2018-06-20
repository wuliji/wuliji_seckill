package com.wuliji.seckill.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wuliji.seckill.domain.SeckillOrder;
import com.wuliji.seckill.domain.SeckillUser;
import com.wuliji.seckill.rabbitmq.MQSender;
import com.wuliji.seckill.rabbitmq.SeckillMessage;
import com.wuliji.seckill.redis.GoodsKey;
import com.wuliji.seckill.redis.RedisService;
import com.wuliji.seckill.result.CodeMsg;
import com.wuliji.seckill.result.Result;
import com.wuliji.seckill.service.GoodsService;
import com.wuliji.seckill.service.OrderService;
import com.wuliji.seckill.service.SeckillService;
import com.wuliji.seckill.vo.GoodsVo;

@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean{
	
	@Autowired
	private GoodsService goodsService;
	
	@Autowired
	private SeckillService seckillService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private RedisService redisService;

	@Autowired
	private MQSender sender;
	
	private Map<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();
	
	/**
	 * QPS 1300 5000并发 10次
	 * 队列优化过后QPS:2114
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value="/do_seckill",method=RequestMethod.POST)
	@ResponseBody
	public Result<Integer> seckill(Model model, SeckillUser user,
			@RequestParam("goodsId")long goodsId) {
		model.addAttribute("user", user);
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		//内存标记，减少redis访问
		Boolean over = localOverMap.get(goodsId);
		if(over) {
			return Result.error(CodeMsg.SECKILL_OVER);
		}
		//预减数量商品秒杀后缓冲中的剩余数量
		Long stock = redisService.decr(GoodsKey.getSeckillGoodsNumber, ""+goodsId);
		if(stock < 0) {
			localOverMap.put(goodsId, true);
			return Result.error(CodeMsg.SECKILL_OVER);
		}
		//判断是否已经秒杀
		SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
		if(order != null) {
			return Result.error(CodeMsg.SECKILL_REPEAT);
		}
		//入队
		SeckillMessage sm = new SeckillMessage();
		sm.setUser(user);
		sm.setGoodsId(goodsId);
		sender.sendSeckillMessage(sm);
		//返回排队中
		return Result.success(0);
		/*
		 * 未使用rabbitmq代码
		 * //判断库存
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
		*/
		
	}
	
	/**
	 * 秒杀轮询是否成功
	 * 成功则返回orderId 库存不足返回-1  未处理完返回0
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value="/result",method=RequestMethod.GET)
	@ResponseBody
	public Result<Long> seckillResult(Model model, SeckillUser user,
			@RequestParam("goodsId")long goodsId) {
		model.addAttribute("user", user);
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		long result = seckillService.getSeckillResult(user.getId(), goodsId);
		return Result.success(result);
	}

	/**
	 * 系统初始化
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> listGoodsVo = goodsService.listGoodsVo();
		if(listGoodsVo == null) {
			return ;
		}
		for (GoodsVo goodsVo : listGoodsVo) {
			redisService.set(GoodsKey.getSeckillGoodsNumber, ""+goodsVo.getId(), goodsVo.getGoodsStock());
			localOverMap.put(goodsVo.getId(), false);
		}
	}
}
