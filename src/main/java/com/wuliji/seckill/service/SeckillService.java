package com.wuliji.seckill.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wuliji.seckill.domain.OrderInfo;
import com.wuliji.seckill.domain.SeckillOrder;
import com.wuliji.seckill.domain.SeckillUser;
import com.wuliji.seckill.redis.RedisService;
import com.wuliji.seckill.redis.SeckillKey;
import com.wuliji.seckill.utils.MD5Utils;
import com.wuliji.seckill.utils.UUIDUtils;
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

	/**
	 * 验证接口地址
	 * @param user
	 * @param goodsId
	 * @param path
	 * @return
	 */
	public boolean checkPath(SeckillUser user, long goodsId, String path) {
		if(user == null || path == null) {
			return false;
		}
		String pathOld = redisService.get(SeckillKey.getSeckillPath, user.getId()+"_"+goodsId, String.class);
		return path.equals(pathOld);
	}

	/**
	 * 生成秒杀地址
	 * @return
	 */
	public String createSeckillPath(SeckillUser user, long goodsId) {
		String str = MD5Utils.md5(UUIDUtils.uuid() + "123456");
		redisService.set(SeckillKey.getSeckillPath, user.getId()+"_"+goodsId, str);
		return str;
	}

	/**
	 * 生成图形验证码
	 * @param user
	 * @param goodsId
	 * @return
	 */
	public BufferedImage createVerifyCode(SeckillUser user, long goodsId) {
		if(user == null || goodsId <=0) {
			return null;
		}
		int width = 80;
		int height = 32;
		//create the image
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		// set the background color
		g.setColor(new Color(0xDCDCDC));
		g.fillRect(0, 0, width, height);
		// draw the border
		g.setColor(Color.black);
		g.drawRect(0, 0, width - 1, height - 1);
		// create a random instance to generate the codes
		Random rdm = new Random();
		// make some confusion
		for (int i = 0; i < 50; i++) {
			int x = rdm.nextInt(width);
			int y = rdm.nextInt(height);
			g.drawOval(x, y, 0, 0);
		}
		// generate a random code
		String verifyCode = generateVerifyCode(rdm);
		g.setColor(new Color(0, 100, 0));
		g.setFont(new Font("Candara", Font.BOLD, 24));
		g.drawString(verifyCode, 8, 24);
		g.dispose();
		//把验证码存到redis中
		int rnd = calc(verifyCode);
		redisService.set(SeckillKey.getVerifyCode, user.getId()+","+goodsId, rnd);
		//输出图片	
		return image;
	}


	private static char[] ops = new char[] {'+','-','*'};
	
	/**
	 * 生成数学公式表 + - *
	 * @param rdm
	 * @return
	 */
	private String generateVerifyCode(Random rdm) {
		int num1 = rdm.nextInt(10);//生成0-10中的随机整数
		int num2 = rdm.nextInt(10);
		int num3 = rdm.nextInt(10);
		char op1 = ops[rdm.nextInt(3)];//生成+-*三个符号中的随机符号
		char op2 = ops[rdm.nextInt(3)];
		String exp = "" +num1 + op1 + num2 + op2 + num3 ;
		return exp;
	}
	
	/**
	 * 计算表达式结果 
	 * @param verifyCode
	 * @return
	 */
	private int calc(String exp) {
		try {
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");
			return (Integer)engine.eval(exp);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 校验图像验证码
	 * @param user
	 * @param goodsId
	 * @param verifyCode
	 * @return
	 */
	public boolean checkVerifyCode(SeckillUser user, long goodsId, int verifyCode) {
		if(user == null || goodsId <=0) {
			return false;
		}
		Integer codeOld = redisService.get(SeckillKey.getVerifyCode, user.getId()+","+goodsId, Integer.class);
		if(codeOld == null || codeOld - verifyCode != 0) {
			return false;
		}
		redisService.delete(SeckillKey.getVerifyCode, user.getId()+","+goodsId);
		return true;
	}

}
