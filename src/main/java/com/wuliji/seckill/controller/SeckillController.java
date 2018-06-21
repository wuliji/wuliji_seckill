package com.wuliji.seckill.controller;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wuliji.seckill.domain.SeckillOrder;
import com.wuliji.seckill.domain.SeckillUser;
import com.wuliji.seckill.rabbitmq.MQSender;
import com.wuliji.seckill.rabbitmq.SeckillMessage;
import com.wuliji.seckill.redis.AccessKey;
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
	@RequestMapping(value="/{path}/do_seckill",method=RequestMethod.POST)
	@ResponseBody
	public Result<Integer> seckill(Model model, SeckillUser user,
			@RequestParam("goodsId")long goodsId,
			@PathVariable("path") String path) {
		model.addAttribute("user", user);
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		//验证秒杀地址
		boolean check = seckillService.checkPath(user, goodsId, path);
		if(!check) {
			return Result.error(CodeMsg.REQUEST_ILLGAL);
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
	 * 秒杀接口防刷
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value="/path",method=RequestMethod.GET)
	@ResponseBody
	public Result<String> seckillPath(HttpServletRequest request, SeckillUser user,
			@RequestParam("goodsId")long goodsId,
			@RequestParam(value="verifyCode",defaultValue="0")int verifyCode) {
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		//防刷限流，查询访问次数,5秒钟访问5次
		String uri = request.getRequestURI();
		String key = uri + "_" + user.getId();
		Integer count = redisService.get(AccessKey.access, key, Integer.class);
		if(count == null) {
			redisService.set(AccessKey.access, key, 1);
		}else if(count < 5){
			redisService.incr(AccessKey.access, key);
		}else {
			return Result.error(CodeMsg.ACCESS_LIMIT);
		}
		//验证图像验证码是否正确
		boolean check = seckillService.checkVerifyCode(user, goodsId, verifyCode);
		if(!check) {
			return Result.error(CodeMsg.SEKILL_CODE_ERROR);
		}
		String path = seckillService.createSeckillPath(user, goodsId);
		return Result.success(path);
	}
	
	/**
	 * 图形验证码
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value="/verifyCode",method=RequestMethod.GET)
	@ResponseBody
	public Result<String> verifyCode(HttpServletResponse respnose, Model model, SeckillUser user,
			@RequestParam("goodsId")long goodsId) {
		model.addAttribute("user", user);
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		BufferedImage image = seckillService.createVerifyCode(user, goodsId);
		try {
			ServletOutputStream sos = respnose.getOutputStream();
			ImageIO.write(image, "JPEG", sos);//将图片写到页面中去
			sos.flush();
			sos.close();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(CodeMsg.SECKILL_FAIL);
		}
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
