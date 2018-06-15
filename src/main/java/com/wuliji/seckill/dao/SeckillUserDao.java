package com.wuliji.seckill.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.wuliji.seckill.domain.SeckillUser;

@Mapper
public interface SeckillUserDao {
	
	@Select("select * from seckill_user where id = #{id}")
	public SeckillUser getById(@Param("id")long id);
	
	@Update("update seckill_user set password = #{pwassword} where id = #{id}")
	public void update(SeckillUser toUpdate);
	
}
