package com.wuliji.seckill.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MQConfig {
	
	public static final String QUEUE = "queue";
	public static final String TOPIC_QUEUE1 = "topic.queue1";
	public static final String TOPIC_QUEUE2 = "topic.queue2";
	public static final String HEADER_QUEUE = "header.queue";
	public static final String TOPIC_EXCHANGE = "topicExchange";
	public static final String FANOUT_EXCHANGE = "fanoutExchange";
	public static final String HEADER_EXCHANGE = "headerExchange";
	
	/**
	 * Direct模式 交换机Exchange
	 * @param message
	 */
	@Bean
	public Queue queue() {
		return new Queue(QUEUE, true);
	}
	
	/**
	 * Topic模式 交换机Exchange
	 * 对绑定相应的消息队列和标识进行消息放入
	 */
	@Bean
	public Queue topicQueue1() {
		return new Queue(TOPIC_QUEUE1, true);
	}
	
	@Bean
	public Queue topicQueue2() {
		return new Queue(TOPIC_QUEUE2, true);
	}
	
	@Bean
	public TopicExchange topicExchange() {
		return new TopicExchange(TOPIC_EXCHANGE);
	}
	
	@Bean
	public Binding topicBinding1() {
		return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("topic.key1");
	}
	
	@Bean
	public Binding topicBinding2() {
		return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("#");
	}
	
	/**
	 * Fanout模式 交换机Exchange
	 * 广播模式，对统一消息进行广播
	 */
	@Bean
	public FanoutExchange fanoutExchange() {
		return new FanoutExchange(FANOUT_EXCHANGE);
	}
	@Bean
	public Binding fanoutBinding1() {
		return BindingBuilder.bind(topicQueue1()).to(fanoutExchange());
	}
	@Bean
	public Binding fanoutBinding2() {
		return BindingBuilder.bind(topicQueue2()).to(fanoutExchange());
	}
	
	/**
	 * Header模式 交换机Exchange
	 * 必须满足map中的参数值相同才将消息放入队列中
	 */
	@Bean
	public HeadersExchange headerExchange() {
		return new HeadersExchange(HEADER_EXCHANGE);
	}
	@Bean
	public Queue headerQueue() {
		return new Queue(HEADER_QUEUE, true);
	}
	@Bean
	public Binding headerBinding() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("header1", "value1");
		map.put("header2", "value2");
		return BindingBuilder.bind(topicQueue1()).to(headerExchange()).whereAll(map).match();
	}
}
