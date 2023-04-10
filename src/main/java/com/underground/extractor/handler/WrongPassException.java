package com.underground.extractor.handler;

public class WrongPassException extends Exception{

    public final static String defaultMessage = "Wrong password";
    public WrongPassException(){
        super(defaultMessage);
    }
    public WrongPassException(String message, Throwable cause){
        super(message, cause);
    }
}
