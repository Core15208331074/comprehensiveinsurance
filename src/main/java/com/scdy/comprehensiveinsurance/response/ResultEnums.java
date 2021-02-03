package com.scdy.comprehensiveinsurance.response;

public enum ResultEnums {

    SUCCESS(true, "0000", "请求成功"),
    SUCCESS_SAVE(true, "0001", "保存成功"),
    FAIL(false, "9999", "请求失败"),
    FAIL_SAVE(false, "9998", "保存失败");

    private Boolean success;
    private String code;
    private String msg;

    ResultEnums(Boolean success, String code, String msg) {
        this.success = success;
        this.code = code;
        this.msg = msg;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
