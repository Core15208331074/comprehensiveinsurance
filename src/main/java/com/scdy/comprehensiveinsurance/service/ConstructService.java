package com.scdy.comprehensiveinsurance.service;


import com.scdy.comprehensiveinsurance.model.SensorModel;
import com.scdy.comprehensiveinsurance.model.request.datasDown.DatasResponseModel;
import com.scdy.comprehensiveinsurance.model.request.datasUp.DatasUpModel;
import com.scdy.comprehensiveinsurance.model.request.deviceInfoUp.DeviceInfoUpModel;
import com.scdy.comprehensiveinsurance.model.request.offlineUp.OfflineModel;
import com.scdy.comprehensiveinsurance.model.request.onlineUp.OnlineModel;
import com.scdy.comprehensiveinsurance.model.request.statusUpdate.StatusUpdateModel;

/**
 * 构建传输到MQTT的模型数据.
 */
public interface ConstructService {
    /**
     * 获取设备是否在线/是否正常工作
     * 0-正常，1-离线，2、机器手臂移动距离，3-机器手臂活动状态
     */
    StatusUpdateModel getDevicesStatusUpdate();
    /**
     * 进度条
     * @param ipAddr 摄像头ip
     * @param hasBeenTouring 表示当前已经轮询完成的设备数量，主要是指已经轮询完了的开关柜数量
     * @param curTouringStatus 0表示开始轮询，1表示正在轮询，2：表示轮询结束
     * @return
     */
//    ScheduleModel getSchedule(String ipAddr, int hasBeenTouring, int curTouringStatus) throws Exception;

    /**
     * 构建上线模型.
     *
     * @return
     */
    OnlineModel getOnline();
    /**
     * 构建下线模型.
     *
     * @return
     */
    OfflineModel getOffline();
    /**
     * 构建设备模型
     *
     * @return
     */
    DeviceInfoUpModel getDeviceInfo();
    /**
     * 构建数据模型.
     *
     * @return
     */
    DatasUpModel getDatasUp(SensorModel sensorModel) throws Exception;
    /**
     * 构建心跳包
     */
    DatasResponseModel getHeartbeat(String code,String message,String tag);
    /**
     * 根据设备id查询设备信息
     */
//    DatasResponseModel getDataResponse(String deviceId) throws Exception;


}
