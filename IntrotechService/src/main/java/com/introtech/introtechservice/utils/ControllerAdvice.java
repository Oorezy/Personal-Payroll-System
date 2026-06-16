package com.introtech.introtechservice.utils;

import com.introtech.introtechservice.common.AppResponse;
import com.introtech.introtechservice.exceptions.IntrotechException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AppResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        return new AppResponse<>(errors.getFirst());
    }

    @ExceptionHandler(IntrotechException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AppResponse<?> handleIntrotechException(IntrotechException ex) {
        return new AppResponse<>(ex.getMessage());
    }

}
