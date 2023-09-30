package com.framework.common.exception;

import com.framework.common.AjaxResult;
import com.framework.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * @version V1.0
 * @program: teamwork
 * @package: com.framework.common.exception
 * @description: Unified exception handling class
 * @create: 2020-06-27 12:58
 **/
@Slf4j
@Component
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * custom exception
     *
     * @param e exception
     * @return
     */
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public AjaxResult handleBaseException(CustomException e) {
        log.debug("exception information：{}", e.getMessage());
        return AjaxResult.warn(e.getMessage());
    }

    /**
     * Controller Related exceptions on the previous layer
     *
     * @param e exception
     * @return exception result
     */
    @ExceptionHandler(value = {
            NoHandlerFoundException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            MissingPathVariableException.class,
            MissingServletRequestParameterException.class,
            TypeMismatchException.class,
            HttpMessageNotReadableException.class,
            HttpMessageNotWritableException.class,
    })
    @ResponseBody
    public AjaxResult handleServletException(Exception e) {
        log.error(e.getMessage(), e);
        if (e instanceof NoHandlerFoundException) {
            return new AjaxResult(ResultCode.NOT_FOUND);
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            return new AjaxResult(ResultCode.METHOD_NOT_ALLOWED);
        } else if (e instanceof HttpMediaTypeNotSupportedException) {
            return new AjaxResult(ResultCode.JSON_FORMAT_ERROR);
        } else if (e instanceof MissingPathVariableException) {
            return new AjaxResult(ResultCode.NOT_FOUND);
        } else if (e instanceof MissingServletRequestParameterException) {
            return new AjaxResult(ResultCode.BAD_REQUEST);
        } else if (e instanceof TypeMismatchException) {
            return new AjaxResult(ResultCode.BAD_REQUEST);
        } else if (e instanceof HttpMessageNotReadableException) {
            return new AjaxResult(ResultCode.JSON_FORMAT_ERROR);
        } else {
            return new AjaxResult(ResultCode.JSON_FORMAT_ERROR);
        }
    }

    /**
     * Parameter binding exception
     *
     * @param e exception
     * @return exception result
     */
    @ExceptionHandler(BindException.class)
    @ResponseBody
    public AjaxResult handleBindException(BindException e) {
        log.error("Parameter binding verification exception", e);
        return wrapperBindingResult(e.getBindingResult());
    }

    /**
     * Parameter verification exception, combine all exceptions that fail the verification into one error message
     *
     * @param e abnormal
     * @return abnormal result
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public AjaxResult handleValidException(MethodArgumentNotValidException e) {
        log.error("Parameter binding verification exception", e);
        return wrapperBindingResult(e.getBindingResult());
    }

    /**
     * Wrapper binding exception result
     *
     * @param bindingResult binding result
     * @return exception resul
     */
    private AjaxResult wrapperBindingResult(BindingResult bindingResult) {
        StringBuilder msg = new StringBuilder();
        for (ObjectError error : bindingResult.getAllErrors()) {
            if (error instanceof FieldError) {
                msg.append(((FieldError) error).getField()).append(": ");
            }
            msg.append(error.getDefaultMessage() == null ? "" : error.getDefaultMessage());
            msg.append(", ");
        }
        return new AjaxResult(ResultCode.BAD_REQUEST, msg.toString());
    }

    /**
     * username or password is incorrect
     *
     * @param e exception
     * @return exception result
     */
    @ExceptionHandler(value = BadCredentialsException.class)
    @ResponseBody
    public AjaxResult handleException(BadCredentialsException e) {
        e.printStackTrace();
        log.debug(e.getMessage(), e);
        return new AjaxResult(ResultCode.USER_ERROR_EXCEPTION);
    }

    /**
     * undefined exception
     *
     * @param e exception
     * @return exception result
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public AjaxResult handleException(Exception e) {
        e.printStackTrace();
        log.debug(e.getMessage(), e);
        return new AjaxResult(ResultCode.INTERNAL_SERVER_ERROR);
    }
}
