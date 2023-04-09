package com.underground.extractor.handler;

public class WrongPassException extends Exception{

    public WrongPassException(String message, Throwable cause){
        super(message, cause);
    }
}
