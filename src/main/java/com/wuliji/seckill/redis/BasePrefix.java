package com.wuliji.seckill.redis;

public abstract class BasePrefix implements KeyPrefix{

	private int expireSeconds;
	
	private String prefix;
	
	public BasePrefix(String prefix) {//0代表永不过期
		this(0, prefix);
	}
	
	public BasePrefix(int expireSeconds, String prefix) {
		this.expireSeconds = expireSeconds;
		this.prefix = prefix;
	}
	
	@Override
	public int expireSeconds() {//默认0代表永不过期
		
		return expireSeconds;
	}

	@Override
	public String getPrefix() {//获取键的前缀
		String className = getClass().getSimpleName();//获取类名
		return className + ":" + prefix;
	}
	
}
