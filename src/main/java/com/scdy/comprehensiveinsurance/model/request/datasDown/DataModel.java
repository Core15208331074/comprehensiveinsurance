package com.scdy.comprehensiveinsurance.model.request.datasDown;

import com.scdy.comprehensiveinsurance.model.request.datasUp.DatasUpImageInfoModel;
import com.scdy.comprehensiveinsurance.model.request.datasUp.DatasUpParamDataModel;
import lombok.Data;

import java.util.List;

@Data
public class DataModel {
    /**
     * 参数list
     */
    private List<DatasUpParamDataModel> paramDataList;
    /**
     * 图片信息
     */
    private DatasUpImageInfoModel imageInfo;
}
