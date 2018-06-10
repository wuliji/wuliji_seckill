package com.wuliji.seckill.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class RedisPoolFactory {
	
	@Autowired
	RedisConf redisConf;
	
	@Bean
	public JedisPool JedisPoolFactory() {
		JedisPoolConfig poolConf = new JedisPoolConfig();
		poolConf.setMaxIdle(redisConf.getPoolMaxIdle());
		poolConf.setMaxTotal(redisConf.getPoolMaxTotal());
		poolConf.setMaxWaitMillis(redisConf.getPoolMaxWait() * 1000);
		JedisPool jp = new JedisPool(poolConf, redisConf.getHost(), redisConf.getPort()
				, redisConf.getTimeout() * 1000, redisConf.getPassword(), 0);
		return jp;
		
	}
}
