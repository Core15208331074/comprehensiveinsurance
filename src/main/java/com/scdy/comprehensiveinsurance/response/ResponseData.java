package com.scdy.comprehensiveinsurance.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResponseData<T> implements Serializable {
    private boolean success;

    private String code;

    private String msg;

    private T data;


    public ResponseData(Boolean success,String code, String msg, T data) {
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResponseData(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResponseData(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public ResponseData(Boolean success,String code, String msg) {
        this.success = success;
        this.code = code;
        this.msg = msg;
    }

    public ResponseData(ResultEnums resultEnums) {
        this.success = resultEnums.getSuccess();
        this.code = resultEnums.getCode();
        this.msg = resultEnums.getMsg();
    }

    public ResponseData(ResultEnums resultEnums, T data) {
        this.success = resultEnums.getSuccess();
        this.code = resultEnums.getCode();
        this.msg = resultEnums.getMsg();
        this.data = data;
    }

    public ResponseData() {
    }


}
