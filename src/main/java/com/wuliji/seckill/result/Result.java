package com.wuliji.seckill.result;

public class Result <T>{
	private int code;
	private String msg;
	private T data;
	
	private Result(T data2) {
		this.code = 0;
		this.msg = "success";
		this.data = data2;
	}
	private Result(CodeMsg msg2) {
		if(msg2 == null) return;
		this.code = msg2.getCode();
		this.msg = msg2.getMsg();
	}
	public int getCode() {
		return code;
	}
	
	public String getMsg() {
		return msg;
	}
	
	public T getData() {
		return data;
	}
	
	/**
	 * 成功时候的调用
	 * @return
	 */
	public static <T> Result<T> success(T data){
		return new Result<T>(data);
	}
	
	/**
	 * 失败时候的调用
	 * @return
	 */
	public static <T> Result<T> error(CodeMsg msg){
		return new Result<T>(msg);
	}
}
