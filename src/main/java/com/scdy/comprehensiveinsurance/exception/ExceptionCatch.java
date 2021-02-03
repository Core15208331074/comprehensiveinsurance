package com.scdy.comprehensiveinsurance.exception;

import com.google.common.collect.ImmutableMap;
import com.scdy.comprehensiveinsurance.response.ResponseData;
import com.scdy.comprehensiveinsurance.response.ResultEnums;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionCatch {
    //使用EXCEPTIONS存放异常类型和错误代码的映射，ImmutableMap的特点的一旦创建不可改变，并且线程安全
    private static ImmutableMap<Class<? extends Throwable>, ResultEnums> immutableMap;
    //使用builder来构建一个异常类型和错误代码的异常
    protected static ImmutableMap.Builder<Class<? extends Throwable>, ResultEnums> builder = ImmutableMap.builder();

    static {
//        builder.put(ArithmeticException.class, CommonCode.INVALID_PARAM);
    }


    @ExceptionHandler(CustomException.class)
    public ResponseData customException(CustomException e) {
        log.error("catch exception : {}\r\nexception: ", e.getMessage(), e);
        ResultEnums resultEnums = e.getResultEnums();
        ResponseData responseData1 = new ResponseData(resultEnums);
        return responseData1;
    }

    @ExceptionHandler(Exception.class)
    public ResponseData exception(Exception e) {
        log.error("catch exception : {}\r\nexception: ", e.getMessage(), e);

        if (StringUtils.isEmpty(immutableMap)) {
            immutableMap = builder.build();
        }

        ResultEnums resultEnums = immutableMap.get(e.getClass());
        ResponseData responseResult;
        if (!StringUtils.isEmpty(resultEnums)) {
            responseResult = new ResponseData(resultEnums);
        } else {
            responseResult = new ResponseData(false,"9999",e.getMessage(),null);
        }
        return responseResult;
    }

}