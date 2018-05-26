package com.lapissea.datamanager;

public class RandomAccessNotSupportedException extends Exception{
	
	public RandomAccessNotSupportedException(){
	}
	
	public RandomAccessNotSupportedException(String message){
		super(message);
	}
	
	public RandomAccessNotSupportedException(String message, Throwable cause){
		super(message, cause);
	}
	
	public RandomAccessNotSupportedException(Throwable cause){
		super(cause);
	}
	
	public RandomAccessNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace){
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
