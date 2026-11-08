package com.danzo.multi_currency_expense_tracker.common;


import lombok.Getter;
import org.springframework.http.HttpStatusCode;
@Getter
public class CustomException extends RuntimeException {
    private final HttpStatusCode httpStatusCode;

    public CustomException(String message,HttpStatusCode httpStatusCode){
        super(message);
        this.httpStatusCode = httpStatusCode;
    }





}
