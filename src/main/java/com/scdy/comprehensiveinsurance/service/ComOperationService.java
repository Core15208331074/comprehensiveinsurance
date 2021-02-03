package com.scdy.comprehensiveinsurance.service;

import com.scdy.comprehensiveinsurance.model.ComRequestModel;
import com.scdy.comprehensiveinsurance.model.PageModel;
import com.scdy.comprehensiveinsurance.model.SensorModel;

import java.util.HashMap;
import java.util.List;

/**
 * 物理串口.
 */
public interface ComOperationService {
    /**
     * 根据串口id获取传感器分页数据.
     *
     * @param comRequestModel
     * @return
     */
    PageModel<SensorModel> getSensorPageData(ComRequestModel comRequestModel) throws Exception;
    /**
     * 根据串口id获取传感器分页数据.
     *
     * @param comId     串口id
     * @param pageIndex 页码
     * @param pageSize  每页查询多少条
     * @return
     */
    PageModel<SensorModel> getSensorPageData(String comId, int pageIndex, int pageSize) throws Exception;

    /**
     * 根据串口id获取传感器实时数据.
     *
     * @param comId 串口id
     * @return
     */
    SensorModel getSensorRealtimeDataByComId(String comId) throws Exception;

//    /**
//     * 轮询
//     *
//     * @param comEntity
//     */
//    void polling(ComEntity comEntity) throws Exception;

    List<String> getComList();

    /**
     * 启动或停止串口.
     *
     * @param serialPortId 串口id
     * @param isStart
     * @return
     */
    Boolean startOrStopComById(String serialPortId, boolean isStart) throws Exception;

    /**
     * 启动串口.
     * <p>
     * 根据id找到串口名启动串口
     *
     * @param serialPortId
     * @return
     */
    Boolean startComById(String serialPortId) throws Exception;

    /**
     * 停止串口.
     * <p>
     * 根据id找到串口名停止串口
     *
     * @param serialPortId
     * @return
     */
    Boolean stopComById(String serialPortId);

    /**
     * 串口配置
     *
     * @return
     */
    HashMap<String, Object> getComConfigData();
}
