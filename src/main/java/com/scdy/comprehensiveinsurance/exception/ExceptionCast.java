package com.scdy.comprehensiveinsurance.exception;

import com.scdy.comprehensiveinsurance.response.ResultEnums;

/**
 * 异常抛出类
 */
public class ExceptionCast {

    //使用此静态方法抛出自定义异常
    public static void cast(ResultEnums resultEnums) {
        throw new CustomException(resultEnums);
    }
}