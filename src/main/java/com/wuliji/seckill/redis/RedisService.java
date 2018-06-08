package com.wuliji.seckill.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class RedisService {
	
	@Autowired
	JedisPool jedisPool;
	
	@Autowired
	RedisConf redisConf;
	
	public <T> T get(String key, Class<T> clazz) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
		}finally {
			returnToPool(jedis);
		}
	}
	
	private void returnToPool(Jedis jedis) {
		if(jedis != null) {
			jedis.close();
		}
	}

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
