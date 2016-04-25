package com.harvey.common.core.beetl;

public class ParseException extends RuntimeException {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ParseException(String message){
        super(message);
    }
    
    public ParseException(String message,Throwable throwable){
        super(message,throwable);
    }
}
