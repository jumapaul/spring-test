package com.key_store_demo.springtest.exception;

public class CustomerEmailUnavailableException extends RuntimeException {

    public CustomerEmailUnavailableException(String message) {
        super(message);
    }
}