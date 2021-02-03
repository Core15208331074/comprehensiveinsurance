package com.scdy.comprehensiveinsurance.model.request.statusUpdate;

import cn.hutool.json.JSONObject;
import lombok.Data;

/**
 * 设备状态上传模型.
 * 如IR摄像头软件是否工作正常，正常表示在线，非正常工作表示离线
 */
@Data
public class StatusUpdateModel {
    /**
     * 信息包
     */
    private JSONObject packet;
    /**
     * 使用MD5对packet值进行校验，该字段存储MD5摘要值
     */
    private String verify;
}
