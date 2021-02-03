package com.scdy.comprehensiveinsurance.model.request.datasUp;

import lombok.Data;

@Data
public class DatasUpImageInfoModel {
    /**
     * 图片随机id
     */
    private String id="";
    /**
     * 图片名称，规则：设备名称+时间戳
     */
    private String name="";
    private String deviceId="";
    /**
     * 图片后缀
     */
    private String suffix="";
    /**
     * 图片文件base64编码
     */
    private String file="";
}
