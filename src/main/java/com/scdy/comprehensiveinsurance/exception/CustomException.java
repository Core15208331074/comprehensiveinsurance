package com.scdy.comprehensiveinsurance.exception;

import com.scdy.comprehensiveinsurance.response.ResultEnums;
import lombok.Data;

/**
 * 自定义异常类
 */
@Data
public class CustomException extends RuntimeException {
    private ResultEnums resultEnums;

    public CustomException(ResultEnums resultEnums) {
        //异常信息为错误代码+异常信息
        super("错误代码：" + resultEnums.getCode() + ",错误信息：" + resultEnums.getMsg());
        this.resultEnums = resultEnums;
    }
}