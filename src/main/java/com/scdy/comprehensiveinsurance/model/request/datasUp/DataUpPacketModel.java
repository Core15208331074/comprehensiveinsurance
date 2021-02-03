package com.scdy.comprehensiveinsurance.model.request.datasUp;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DataUpPacketModel {
    /**
     * 信息包id
     */
    private String id;
    /**
     * 参数信息
     */
    private List<DatasUpParamDataModel> datas;
    /**
     * 图片信息
     */
    private List<DatasUpImageInfoModel> images =new ArrayList<>();
    /**
     * 时间
     */
    private String time;
}
