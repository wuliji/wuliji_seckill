package com.wuliji.seckill.exception;


import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wuliji.seckill.result.CodeMsg;
import com.wuliji.seckill.result.Result;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
	
	@ExceptionHandler(value=Exception.class)
	public Result<String> exceptionHandler(HttpServletRequest request, Exception e){
		
		if(e instanceof GlobalException) {
			GlobalException ex = (GlobalException) e;
			CodeMsg msg = ex.getCm();
			return Result.error(msg);
		}
		
		if(e instanceof BindException) {
			BindException ex = (BindException) e;
			List<ObjectError> errors = ex.getAllErrors();
			ObjectError error = errors.get(0);
			String message = error.getDefaultMessage();
			return Result.error(CodeMsg.BIND_ERROR.fillArgs(message));
		}else {
			return Result.error(CodeMsg.SERVER_ERROR);
		}
	}
}
