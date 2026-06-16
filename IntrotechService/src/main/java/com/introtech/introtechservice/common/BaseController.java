package com.introtech.introtechservice.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;

public abstract class BaseController<T extends Model, ID>  {

    private static final Logger log = LoggerFactory.getLogger(BaseController.class);

    public <M> AppResponse<M> toAppResponse(M data) {
        return toAppResponse("Success", data);
    }

    public <M> AppResponse<M> toAppResponse(String message, M data) {
        return toAppResponse(true, message, data);
    }

    public <M> AppResponse<M> toAppResponse(boolean status, String message, M data) {
        return new AppResponse<>(status, message, data);
    }

    public AppResponse<?> toErrorAppResponse(String message) {
        return toAppResponse(false, message, null);
    }


    @ExceptionHandler(Exception.class)
    public AppResponse<?> handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return toErrorAppResponse(ex.getMessage());
    }
}
