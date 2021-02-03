package com.scdy.comprehensiveinsurance.response;

public class ResponseDataUtil {
    /**
     * 带实体的统一返回
     *
     * @param data 实体
     * @param <T>  实体类型
     * @return
     */
    public static <T> ResponseData buildSuccess(T data) {
        return new ResponseData<T>(ResultEnums.SUCCESS, data);
    }
 
    public static ResponseData buildSuccess() {
        return new ResponseData(ResultEnums.SUCCESS);
    }
 
    public static ResponseData buildSuccess(String msg) {
        return new ResponseData(ResultEnums.SUCCESS.getSuccess(),ResultEnums.SUCCESS.getCode(), msg);
//        return new ResponseData(ResultEnums.SUCCESS.getCode(), msg);
    }
 
    public static ResponseData buildSuccess(String code, String msg) {
        return new ResponseData(code, msg);
    }
 
    public static <T> ResponseData buildSuccess(String code, String msg, T data) {
        return new ResponseData<T>(code, msg, data);
    }
 
    public static ResponseData buildSuccess(ResultEnums resultEnums) {
        return new ResponseData(resultEnums);
    }
 
    public static <T> ResponseData buildError(T data) {
        return new ResponseData<T>(ResultEnums.FAIL, data);
    }
 
    public static ResponseData buildError() {
        return new ResponseData(ResultEnums.FAIL);
    }
 
    public static ResponseData buildError(String msg) {
        return new ResponseData(ResultEnums.FAIL.getSuccess(),ResultEnums.FAIL.getCode(), msg);
//        return new ResponseData(ResultEnums.FAIL.getCode(), msg);
    }
 
    public static ResponseData buildError(String code, String msg) {
        return new ResponseData(code, msg);
    }
 
    public static <T> ResponseData buildError(String code, String msg, T data) {
        return new ResponseData<T>(code, msg, data);
    }
 
    public static ResponseData buildError(ResultEnums resultEnums) {
        return new ResponseData(resultEnums);
    }
}
