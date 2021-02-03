package com.scdy.comprehensiveinsurance.service.impl;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scdy.comprehensiveinsurance.entity.*;
import com.scdy.comprehensiveinsurance.model.PointModel;
import com.scdy.comprehensiveinsurance.model.SensorModel;
import com.scdy.comprehensiveinsurance.model.request.datasDown.DatasResponseDevServiceReplyModel;
import com.scdy.comprehensiveinsurance.model.request.datasDown.DatasResponseModel;
import com.scdy.comprehensiveinsurance.model.request.datasDown.DatasResponseReplyPacketModel;
import com.scdy.comprehensiveinsurance.model.request.datasUp.DataUpPacketModel;
import com.scdy.comprehensiveinsurance.model.request.datasUp.DatasUpModel;
import com.scdy.comprehensiveinsurance.model.request.datasUp.DatasUpParamDataModel;
import com.scdy.comprehensiveinsurance.model.request.deviceInfoUp.*;
import com.scdy.comprehensiveinsurance.model.request.offlineUp.OfflineModel;
import com.scdy.comprehensiveinsurance.model.request.offlineUp.OfflineServiceModel;
import com.scdy.comprehensiveinsurance.model.request.offlineUp.OfflineServicePacketModel;
import com.scdy.comprehensiveinsurance.model.request.onlineUp.OnlineModel;
import com.scdy.comprehensiveinsurance.model.request.onlineUp.OnlineServiceModel;
import com.scdy.comprehensiveinsurance.model.request.onlineUp.OnlineServicePacketModel;
import com.scdy.comprehensiveinsurance.model.request.statusUpdate.DeviceStatusModel;
import com.scdy.comprehensiveinsurance.model.request.statusUpdate.StatusUpPacketModel;
import com.scdy.comprehensiveinsurance.model.request.statusUpdate.StatusUpdateModel;
import com.scdy.comprehensiveinsurance.service.*;
import com.scdy.comprehensiveinsurance.utils.DictUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ConstructServiceImpl implements ConstructService {
    @Autowired
    private ComService comService;
    @Autowired
    private SensorService sensorService;
    @Autowired
    private ParamService paramService;
    @Autowired
    private EthService ethService;
    @Autowired
    private DeviceParamService deviceParamService;

    //    @Autowired
//    private IrCameraService irCameraService;
//    @Autowired
//    private MonitorareaService monitorareaService;
//    @Autowired
//    private MeasureGroupService measureGroupService;
//    @Autowired
//    private GroupAreaService groupAreaService;
//    @Autowired
//    private MeaSureparaService meaSureparaService;
//    @Autowired
//    private MonitorGroupService monitorGroupService;
    @Autowired
    private DictUtil dictUtil;
//    @Autowired
//    private DeviceMonitordeviceService deviceMonitordeviceService;
//    @Autowired
//    private MonitordeviceService monitordeviceService;

        /**
     * 服务id
     */
    private String serverId = null;
    @Value("${device.disconnect.time}")
    private Integer time;
    /**
     * 服务名称
     */
    private String serverName = null;
    /**
     * 服务描述
     */
    private String serverDescription = null;
    /**
     * 服务版本号
     */
    private String severVersion = null;
    /**
     * 服务类型
     */
    private String serverType = null;
    /**
     * 服务类型
     */
    private String operation = null;

    @Override
    public StatusUpdateModel getDevicesStatusUpdate() {
        StatusUpdateModel statusUpdateModel = new StatusUpdateModel();
        ArrayList<DeviceStatusModel> deviceStatusModels = new ArrayList<>();//参数信息List
        QueryWrapper<EthEntity> ethEntityQueryWrapper = new QueryWrapper<>();
        ethEntityQueryWrapper.eq("is_deleted", 0);
        List<EthEntity> ethEntityList = ethService.list(ethEntityQueryWrapper);
        for (EthEntity ethEntity : ethEntityList) {
            DeviceStatusModel deviceStatusModel = new DeviceStatusModel();
            deviceStatusModel.setId(ethEntity.getId());//设备id
            String sensorModel = ethEntity.getSensorModel();
            QueryWrapper<SensorEntity> sensorEntityQueryWrapper = new QueryWrapper<>();
            sensorEntityQueryWrapper.eq("is_deleted", 0);
            sensorEntityQueryWrapper.eq("sensor_name_code", sensorModel);
            SensorEntity sensorEntity = sensorService.getOne(sensorEntityQueryWrapper);
            deviceStatusModel.setName(sensorEntity.getSensorName());
            LocalDateTime updateTime = ethEntity.getUpdateTime();
            DateTime nowTime = DateUtil.dateSecond();
            long betweenMinute = DateUtil.between(DateUtil.date(updateTime), nowTime, DateUnit.MINUTE);
            if (betweenMinute > time) {
                deviceStatusModel.setStatus(1);//0-正常，1-离线等
            } else {
                deviceStatusModel.setStatus(0);//0-正常，1-离线等
            }
            String dictValue = dictUtil.getDictValue("sys", "influence");
            deviceStatusModel.setInfluence(Integer.parseInt(dictValue));//0-本体，1-同组，影响范围
            deviceStatusModels.add(deviceStatusModel);
        }
        StatusUpPacketModel statusUpPacketModel = new StatusUpPacketModel();//信息包
        statusUpPacketModel.setId(IdUtil.simpleUUID());//信息包id
        statusUpPacketModel.setDevsStatus(deviceStatusModels);//参数信息
        statusUpPacketModel.setTime(DateUtil.now());
        JSONObject packet = JSONUtil.parseObj(statusUpPacketModel);
        statusUpdateModel.setPacket(packet);//信息包
        String s = SecureUtil.md5(packet.toString());
        statusUpdateModel.setVerify(s);//使用MD5对packet值进行校验，该字段存储MD5摘要值
        return statusUpdateModel;
    }


    @Override
    public DatasResponseModel getHeartbeat(String code, String message, String tag) {
        DatasResponseModel datasResponseModel = new DatasResponseModel();


        DatasResponseReplyPacketModel datasResponseReplyPacketModel = new DatasResponseReplyPacketModel();
        datasResponseReplyPacketModel.setId(IdUtil.simpleUUID());//信息包id

        DatasResponseDevServiceReplyModel datasResponseDevServiceReplyModel = new DatasResponseDevServiceReplyModel();


        datasResponseDevServiceReplyModel.setId(dictUtil.getDictValue("mqtt", "server_id"));//机器人服务id,可做认证使用
        datasResponseDevServiceReplyModel.setCode(code);//状态码：100：已接收，200：已执行，300：已完成
        datasResponseDevServiceReplyModel.setMessage(message);//消息
        datasResponseDevServiceReplyModel.setTag(tag);//0-心跳，1-查询,2-控制

        datasResponseReplyPacketModel.setDevServiceReply(datasResponseDevServiceReplyModel);//应答
        datasResponseReplyPacketModel.setTime(DateUtil.now());//时间

        JSONObject jsonObject = JSONUtil.parseObj(datasResponseReplyPacketModel);
        datasResponseModel.setPacket(jsonObject);//信息包
        datasResponseModel.setVerify(SecureUtil.md5(jsonObject.toString()));//使用MD5对packet值进行校验，该字段存储MD5摘要值
        return datasResponseModel;
    }


//    @Override
//    public ScheduleModel getSchedule(String ipAddr, int hasBeenTouring, int curTouringStatus) throws Exception {
//        ScheduleModel scheduleModel = new ScheduleModel();
//        SchedulePacketModel packetModel = new SchedulePacketModel();
//
//        //摄像头设备的uuid
//        Ircamera camera = getCamera(ipAddr);
//        String deviceUUID = camera.getDeviceUUID();
//        packetModel.setId(deviceUUID);
//
//
//
//
//
//        //当前程序需要轮询的设备总体数量，主要是指开关柜的所有数量
//        int monitorNum = getMonitorNum(ipAddr);
//        packetModel.setDevTotal(monitorNum);
//
//        //表示当前已经轮询完成的设备数量，主要是指已经轮询完了的开关柜数量
//        packetModel.setHasBeenTouring(hasBeenTouring);
//
//        List<CurTouringDevModel> curTouringDevs = new ArrayList<CurTouringDevModel>();
//
//        CurTouringDevModel curTouringDevModel = new CurTouringDevModel();
//
//        //开关柜的设备uuid
//        DeviceMonitordeviceEntity deviceMonitordevice = getDeviceMonitordevice(ipAddr);
//        String monitorDeviceUuid = deviceMonitordevice.getMonitorDeviceUuid();
//        curTouringDevModel.setCurTouringDevId(monitorDeviceUuid);
//
//        //0表示开始轮询，1表示正在轮询，2：表示轮询结束
//        curTouringDevModel.setCurTouringStatus(curTouringStatus);
//
//        curTouringDevs.add(curTouringDevModel);
//
//        packetModel.setCurTouringDevs(curTouringDevs);
//
//        packetModel.setTime(DateUtil.format(DateUtil.date(), "yyyy-MM-dd HH:mm:ss.S"));
//
//
//        scheduleModel.setPacket(packetModel);
//        scheduleModel.setVerify(SecureUtil.md5(packetModel.toString()));
//        return scheduleModel;
//    }

//    private DeviceMonitordeviceEntity getDeviceMonitordevice(String ipAddr){
//        //获取摄像头deviceUUID
//        Ircamera camera = getCamera(ipAddr);
//        String deviceUUID = camera.getDeviceUUID();
//
//        //设备柜体的uuid号
//        QueryWrapper<DeviceMonitordeviceEntity> deviceMonitordeviceEntityQueryWrapper = new QueryWrapper<>();
//        deviceMonitordeviceEntityQueryWrapper.eq("device_uuid", deviceUUID);
//        List<DeviceMonitordeviceEntity> deviceMonitordeviceEntitieList = deviceMonitordeviceService.list(deviceMonitordeviceEntityQueryWrapper);
//        for (DeviceMonitordeviceEntity deviceMonitordeviceEntity : deviceMonitordeviceEntitieList) {
//           return deviceMonitordeviceEntity;
//        }
//        return null;
//    }

//    /**
//     * 根据摄像头ip获取被检查数量
//     *
//     * @param ipAddr
//     * @return
//     */
//    public int getMonitorNum(String ipAddr) throws Exception {
//        Ircamera camera = getCamera(ipAddr);
//        String deviceUUID = camera.getDeviceUUID();
//
//        QueryWrapper<Monitorarea> monitorareaQueryWrapper = new QueryWrapper<>();
//        monitorareaQueryWrapper.eq("DeviceUUID", deviceUUID);
//        List<Monitorarea> monitorareaList = monitorareaService.list(monitorareaQueryWrapper);
//        int size = monitorareaList.size();
//
//        QueryWrapper<MeasureGroup> measureGroupQueryWrapper = new QueryWrapper<>();
//        measureGroupQueryWrapper.eq("DeviceUUID",deviceUUID);
//        List<MeasureGroup> measureGroupList = measureGroupService.list(measureGroupQueryWrapper);
//        int size1 = measureGroupList.size();
//        return size+size1;
//    }

//    /**
//     * 根据摄像头ip获取摄像头信息
//     *
//     * @param ipAddr
//     * @return
//     */
//    private Ircamera getCamera(String ipAddr) {
//        QueryWrapper<Ircamera> ircameraQueryWrapper = new QueryWrapper<>();
//        ircameraQueryWrapper.eq("IPAddr", ipAddr);
//        List<Ircamera> ircameraList = irCameraService.list(ircameraQueryWrapper);
//        for (Ircamera ircamera : ircameraList) {
//            return ircamera;
//        }
//        return null;
//    }


    @Override
    public OnlineModel getOnline() {
        OnlineModel online = new OnlineModel();
        OnlineServicePacketModel servicePacket = new OnlineServicePacketModel();
        servicePacket.setId(IdUtil.simpleUUID());//生成不带-的uuid
        servicePacket.setTime(DateUtil.now());
        OnlineServiceModel onlineServiceModel = new OnlineServiceModel();
         serverId = dictUtil.getDictValue("mqtt", "server_id");
        onlineServiceModel.setId(serverId);
        serverDescription = dictUtil.getDictValue("mqtt", "server_description");
        onlineServiceModel.setDescription(serverDescription);
        severVersion = dictUtil.getDictValue("mqtt", "sever_version");
        onlineServiceModel.setVersion(severVersion);
        serverName = dictUtil.getDictValue("mqtt", "server_name");
        serverType = dictUtil.getDictValue("mqtt", "server_type");
        onlineServiceModel.setType(serverType);
        onlineServiceModel.setName(serverName);
        servicePacket.setService(onlineServiceModel);
        JSONObject jsonObject = JSONUtil.parseObj(servicePacket);
        online.setPacket(jsonObject);
        online.setVerify(SecureUtil.md5(jsonObject.toString()));
        return online;
    }

    @Override
    public OfflineModel getOffline() {
        OfflineModel offline = new OfflineModel();

        OfflineServicePacketModel servicePacket = new OfflineServicePacketModel();
        servicePacket.setId(IdUtil.simpleUUID());//生成不带-的uuid 信息包id
        servicePacket.setTime(DateUtil.now());//传输时间


        OfflineServiceModel offlineService = new OfflineServiceModel();
        offlineService.setId(serverId);//服务id,认证识别码
        offlineService.setDescription(serverDescription);
        offlineService.setVersion(severVersion);
        offlineService.setType(serverType);
        offlineService.setName(serverName);
        servicePacket.setService(offlineService);//服务信息
        JSONObject jsonObject = JSONUtil.parseObj(servicePacket);
        offline.setPacket(jsonObject);//信息包
        offline.setVerify(SecureUtil.md5(jsonObject.toString()));//使用MD5对packet值进行校验，该字段存储MD5摘要值
        return offline;
    }

    @Override
    public DeviceInfoUpModel getDeviceInfo() {
        DeviceInfoUpModel deviceInfoUpModel = new DeviceInfoUpModel();

        DeviceInfoUpPacketModel deviceInfoUpPacketModel = new DeviceInfoUpPacketModel();//信息包
        ArrayList<DeviceInfoUpDeviceInfoModel> deviceInfoUpDeviceInfoModelList = new ArrayList<>();//设备信息列表
        ArrayList<DeviceInfoUpParamInfoModel> deviceInfoUpParamInfoModelList = new ArrayList<>();//参数信息列表
        List<DeviceRelationShipModel> deviceRelationShipModels = new ArrayList<>();//监测设备和被监测设备关系列表


        //构建串口设备信息
        QueryWrapper<ComEntity> comEntityQueryWrapper = new QueryWrapper<>();
        comEntityQueryWrapper.eq("is_deleted", 0);
        List<ComEntity> comEntityList = comService.list(comEntityQueryWrapper);
        for (ComEntity comEntity : comEntityList) {
            String deviceId = comEntity.getId();//设备id

            QueryWrapper<SensorEntity> sensorEntityQueryWrapper = new QueryWrapper<>();
            sensorEntityQueryWrapper.eq("is_deleted", 0);
            sensorEntityQueryWrapper.eq("type", "com");
            sensorEntityQueryWrapper.eq("sensor_name_code", comEntity.getSensorModel());
            SensorEntity sensorEntity = sensorService.getOne(sensorEntityQueryWrapper);

            /**
             * 构建deviceInfo
             */
            DeviceInfoUpDeviceInfoModel deviceInfoUpDeviceInfoModel = new DeviceInfoUpDeviceInfoModel();
            deviceInfoUpDeviceInfoModel.setId(deviceId);//设备id
            deviceInfoUpDeviceInfoModel.setUdid(SecureUtil.md5(comEntity.toString()));//Udid(根据输入的信息生成uuid或者hash)
            deviceInfoUpDeviceInfoModel.setName(sensorEntity.getSensorName());//设备名称,全称 TODO 如果不满意可以改成type_name字段
            deviceInfoUpDeviceInfoModel.setInstallPostion(comEntity.getInstallPostion());//安装位置
            deviceInfoUpDeviceInfoModel.setApplicationType(Integer.parseInt(sensorEntity.getApplicationType()));//应用类:0-输电，1-配电2-变电
            deviceInfoUpDeviceInfoModel.setFuncType(Integer.parseInt(sensorEntity.getFuncType()));//功能类:0-网关设备，1-采集设备，2-电力设备
            deviceInfoUpDeviceInfoModel.setTypeName(sensorEntity.getTypeName());//开关柜/变压器/摄像机 sensorEntity.getTypeName()
//            deviceInfoUpDeviceInfoModel.setMonitoringTypeCode(sensorEntity.getMonitoringTypeCode());//监测类型编码 改到parameter里面去了
            deviceInfoUpDeviceInfoModel.setTag(comEntity.getTag());//标签，主要是传1，2，3...表示序号
            deviceInfoUpDeviceInfoModelList.add(deviceInfoUpDeviceInfoModel);


            /**
             * 构建参数信息
             */
            QueryWrapper<ParamEntity> paramEntityQueryWrapper = new QueryWrapper<>();
            paramEntityQueryWrapper.eq("is_deleted", 0);
            paramEntityQueryWrapper.eq("sensor_name_code", comEntity.getSensorModel());
            List<ParamEntity> paramEntityList = paramService.list(paramEntityQueryWrapper);
            for (ParamEntity paramEntity : paramEntityList) {
                String paramId = paramEntity.getId();//参数id
                DeviceInfoUpParamInfoModel deviceInfoUpParamInfoModel = new DeviceInfoUpParamInfoModel();

                QueryWrapper<DeviceParamEntity> deviceParamEntityQueryWrapper = new QueryWrapper<>();
                deviceParamEntityQueryWrapper.eq("is_deleted",0);
                deviceParamEntityQueryWrapper.eq("device_id",deviceId);
                deviceParamEntityQueryWrapper.eq("param_id",paramId);
                DeviceParamEntity deviceParamEntity = deviceParamService.getOne(deviceParamEntityQueryWrapper);
                deviceInfoUpParamInfoModel.setId(deviceParamEntity.getId());//参数id
                deviceInfoUpParamInfoModel.setName(paramEntity.getParameter());//名称
                if (StringUtils.isEmpty(paramEntity.getTag())) {
                    deviceInfoUpParamInfoModel.setTag("");//同类型器件的序号
                } else {
                    deviceInfoUpParamInfoModel.setTag(comEntity.getTag()+"_"+paramEntity.getTag());//同类型器件的序号
                }

                deviceInfoUpParamInfoModel.setObjType(paramEntity.getParameterCode());//压板、旋钮、指示灯、仪表/电缆终端 paramEntity.getObjType()
                if (StringUtils.isEmpty(paramEntity.getValue())) {
                    deviceInfoUpParamInfoModel.setValue("");//值,开/合，亮/灭，远方/就地
                } else {
                    deviceInfoUpParamInfoModel.setValue(paramEntity.getValue());//值,开/合，亮/灭，远方/就地
                }

                deviceInfoUpParamInfoModel.setDeviceId(comEntity.getId());//参数所属设备 paramEntity.getSensorNameCode()
                switch (paramEntity.getDataType()) {
                    case "遥信":
                        deviceInfoUpParamInfoModel.setOperation("0");//0-遥信，1-遥测，2-遥控，3-遥调
                        break;
                    case "遥测":
                        deviceInfoUpParamInfoModel.setOperation("1");//0-遥信，1-遥测，2-遥控，3-遥调
                        break;
                    case "遥控":
                        deviceInfoUpParamInfoModel.setOperation("2");//0-遥信，1-遥测，2-遥控，3-遥调
                        break;
                    case "控制":
                        deviceInfoUpParamInfoModel.setOperation("2");//0-遥信，1-遥测，2-遥控，3-遥调
                        break;
                    case "遥调":
                        deviceInfoUpParamInfoModel.setOperation("3");//0-遥信，1-遥测，2-遥控，3-遥调
                        break;
                    default:
                        deviceInfoUpParamInfoModel.setOperation(paramEntity.getDataType());//0-遥信，1-遥测，2-遥控，3-遥调
                        break;
                }
                deviceInfoUpParamInfoModel.setMonitoringTypeCode(paramEntity.getMonitoringTypeCode());//监测类型编码
                deviceInfoUpParamInfoModel.setMonitoringItemCode(paramEntity.getMonitoringItemCode());//监测项代码
                deviceInfoUpParamInfoModelList.add(deviceInfoUpParamInfoModel);
            }


            /**
             * 构建监测设备和被监测设备关系
             */
            DeviceRelationShipModel deviceRelationShipModel = new DeviceRelationShipModel();
            deviceRelationShipModel.setDev1(dictUtil.getDictValue("mqtt", "server_id"));//监测设备id 表示规约转换软件id
            deviceRelationShipModel.setActor1(dictUtil.getDictValue("mqtt", "actor1"));//parent
            deviceRelationShipModel.setDev2(comEntity.getId());//被监测设备id 表示eth列表该行id
            deviceRelationShipModel.setActor2(dictUtil.getDictValue("mqtt", "actor2"));//sub
            deviceRelationShipModel.setRelation(Integer.parseInt(sensorEntity.getRelation()));//0:表示直连,1:表示非直连
            deviceRelationShipModels.add(deviceRelationShipModel);
        }

        //构建eth设备信息
        QueryWrapper<EthEntity> ethEntityQueryWrapper = new QueryWrapper<>();
        ethEntityQueryWrapper.eq("is_deleted", 0);
        List<EthEntity> ethEntityList = ethService.list(ethEntityQueryWrapper);
        for (EthEntity ethEntity : ethEntityList) {
            QueryWrapper<SensorEntity> sensorEntityQueryWrapper = new QueryWrapper<>();
            sensorEntityQueryWrapper.eq("is_deleted", 0);
//            sensorEntityQueryWrapper.eq("type", "eth");
            sensorEntityQueryWrapper.eq("sensor_name_code", ethEntity.getSensorModel());
            SensorEntity sensorEntity = sensorService.getOne(sensorEntityQueryWrapper);

            /**
             * 构建deviceInfo
             */
            DeviceInfoUpDeviceInfoModel deviceInfoUpDeviceInfoModel = new DeviceInfoUpDeviceInfoModel();
            deviceInfoUpDeviceInfoModel.setId(ethEntity.getId());//设备id
            deviceInfoUpDeviceInfoModel.setUdid(SecureUtil.md5(ethEntity.toString()));//Udid(根据输入的信息生成uuid或者hash)
            deviceInfoUpDeviceInfoModel.setName(sensorEntity.getSensorName());//设备名称,全称 TODO 如果不满意可以改成type_name字段
            deviceInfoUpDeviceInfoModel.setInstallPostion(ethEntity.getInstallPostion());//安装位置
            deviceInfoUpDeviceInfoModel.setApplicationType(Integer.parseInt(sensorEntity.getApplicationType()));//应用类:0-输电，1-配电2-变电
            deviceInfoUpDeviceInfoModel.setFuncType(Integer.parseInt(sensorEntity.getFuncType()));//功能类:0-网关设备，1-采集设备，2-电力设备
            deviceInfoUpDeviceInfoModel.setTypeName(sensorEntity.getTypeName());//开关柜/变压器/摄像机 sensorEntity.getTypeName()
//            deviceInfoUpDeviceInfoModel.setMonitoringTypeCode(sensorEntity.getMonitoringTypeCode());//监测类型编码 改到parameter里面去了
            deviceInfoUpDeviceInfoModel.setTag(ethEntity.getTag());//标签，主要是传1，2，3...表示序号
            deviceInfoUpDeviceInfoModelList.add(deviceInfoUpDeviceInfoModel);


            /**
             * 构建参数信息
             */
            QueryWrapper<DeviceParamEntity> deviceParamEntityQueryWrapper = new QueryWrapper<>();
            deviceParamEntityQueryWrapper.eq("is_deleted", 0);
            deviceParamEntityQueryWrapper.eq("device_id", ethEntity.getId());
            List<DeviceParamEntity> deviceParamEntityList = deviceParamService.list(deviceParamEntityQueryWrapper);
            for (DeviceParamEntity deviceParamEntity : deviceParamEntityList) {
                String id = deviceParamEntity.getId();//作为参数id
                String paramId = deviceParamEntity.getParamId();
                ParamEntity paramEntity = paramService.getById(paramId);

                DeviceInfoUpParamInfoModel deviceInfoUpParamInfoModel = new DeviceInfoUpParamInfoModel();
                deviceInfoUpParamInfoModel.setId(id);//参数id
                deviceInfoUpParamInfoModel.setName(paramEntity.getParameter());//名称
                if (StringUtils.isEmpty(paramEntity.getTag())) {
                    deviceInfoUpParamInfoModel.setTag("");//同类型器件的序号
                } else {
                    deviceInfoUpParamInfoModel.setTag(ethEntity.getTag()+"_"+paramEntity.getTag());//同类型器件的序号
                }

                deviceInfoUpParamInfoModel.setObjType(paramEntity.getParameterCode());//压板、旋钮、指示灯、仪表/电缆终端 paramEntity.getObjType()
                if (StringUtils.isEmpty(paramEntity.getValue())) {
                    deviceInfoUpParamInfoModel.setValue("");//值,开/合，亮/灭，远方/就地
                } else {
                    deviceInfoUpParamInfoModel.setValue(paramEntity.getValue());//值,开/合，亮/灭，远方/就地
                }

                deviceInfoUpParamInfoModel.setDeviceId(ethEntity.getId());//参数所属设备 paramEntity.getSensorNameCode()
                switch (paramEntity.getDataType()) {
                    case "遥信":
                        deviceInfoUpParamInfoModel.setOperation("0");//0-遥信，1-遥测，2-遥控，3-遥调
                        break;
                    case "遥测":
                        deviceInfoUpParamInfoModel.setOperation("1");//0-遥信，1-遥测，2-遥控，3-遥调
                        break;
                    case "遥控":
                        deviceInfoUpParamInfoModel.setOperation("2");//0-遥信，1-遥测，2-遥控，3-遥调
                        break;
                    case "遥调":
                        deviceInfoUpParamInfoModel.setOperation("3");//0-遥信，1-遥测，2-遥控，3-遥调
                        break;
                    default:
                        deviceInfoUpParamInfoModel.setOperation(paramEntity.getDataType());//0-遥信，1-遥测，2-遥控，3-遥调
                        break;
                }
                deviceInfoUpParamInfoModel.setMonitoringTypeCode(paramEntity.getMonitoringTypeCode());//监测类型编码
                deviceInfoUpParamInfoModel.setMonitoringItemCode(paramEntity.getMonitoringItemCode());//监测项代码
                deviceInfoUpParamInfoModelList.add(deviceInfoUpParamInfoModel);
            }


            /**
             * 构建监测设备和被监测设备关系
             */
            DeviceRelationShipModel deviceRelationShipModel = new DeviceRelationShipModel();
            deviceRelationShipModel.setDev1(dictUtil.getDictValue("mqtt", "server_id"));//监测设备id 表示规约转换软件id
            deviceRelationShipModel.setActor1(dictUtil.getDictValue("mqtt", "actor1"));//parent
            deviceRelationShipModel.setDev2(ethEntity.getId());//被监测设备id 表示eth列表该行id
            deviceRelationShipModel.setActor2(dictUtil.getDictValue("mqtt", "actor2"));//sub
            deviceRelationShipModel.setRelation(Integer.parseInt(sensorEntity.getRelation()));//0:表示直连,1:表示非直连
            deviceRelationShipModels.add(deviceRelationShipModel);
        }


        deviceInfoUpPacketModel.setDevices(deviceInfoUpDeviceInfoModelList);//设备信息
        deviceInfoUpPacketModel.setParams(deviceInfoUpParamInfoModelList);//参数信息
        deviceInfoUpPacketModel.setDeviceRelationship(deviceRelationShipModels);//监测设备和被监测设备关系
        deviceInfoUpPacketModel.setId(IdUtil.simpleUUID());//信息包id，uuid
        deviceInfoUpPacketModel.setTime(DateUtil.now());

        JSONObject jsonObject = JSONUtil.parseObj(deviceInfoUpPacketModel);
        deviceInfoUpModel.setPacket(jsonObject);//信息包
        deviceInfoUpModel.setVerify(SecureUtil.md5(jsonObject.toString()));//使用MD5对packet值进行校验，该字段存储MD5摘要值
        return deviceInfoUpModel;
    }

    @Override
    public DatasUpModel getDatasUp(SensorModel sensorModel) throws Exception {
        DatasUpModel datasUpModel = new DatasUpModel();//创建数据上传包

        DataUpPacketModel dataUpPacketModel = new DataUpPacketModel();//信息包
        dataUpPacketModel.setId(IdUtil.simpleUUID());//信息包id
        List<DatasUpParamDataModel> datasUpParamDataModelList = new ArrayList<>();


        List<PointModel> pointModelList = sensorModel.getPointModelList();
        for (PointModel pointModel : pointModelList) {
            List<PointModel> pointList = pointModel.getPointList();
            for (PointModel model : pointList) {
                String pointName = model.getPointName();

                EthEntity ethServiceById = ethService.getById(model.getEthId());
                DeviceParamEntity deviceParamServiceById = deviceParamService.getById(model.getParamId());
                String paramId = deviceParamServiceById.getParamId();
                ParamEntity paramEntity = paramService.getById(paramId);

                DatasUpParamDataModel paramDataModel = new DatasUpParamDataModel();
                paramDataModel.setId(model.getParamId());//参数id
                paramDataModel.setName(pointName);//参数名称
                if (StringUtils.isEmpty(paramEntity.getTag())) {
                    paramDataModel.setTag("");//参数标签
                } else {
                    paramDataModel.setTag(ethServiceById.getTag()+"_"+paramEntity.getTag());//参数标签
                }

                paramDataModel.setType(paramEntity.getParameterCode());
                paramDataModel.setValue(model.getValue());
                paramDataModel.setDeviceId(model.getEthId());
                if (StringUtils.isEmpty(model.getUnit())) {
                    paramDataModel.setUnit("");
                } else {
                    paramDataModel.setUnit(model.getUnit());
                }

                datasUpParamDataModelList.add(paramDataModel);
            }
        }


        dataUpPacketModel.setDatas(datasUpParamDataModelList);//参数信息
//        dataUpPacketModel.setImageList(null);//图片信息datasUpImageInfoModel
        dataUpPacketModel.setTime(DateUtil.format(DateUtil.toLocalDateTime(DateUtil.dateSecond()), "yyyy-MM-dd HH:mm:ss.S"));//时间

        JSONObject jsonObject = JSONUtil.parseObj(dataUpPacketModel);
        datasUpModel.setPacket(jsonObject);//信息包
        datasUpModel.setVerify(SecureUtil.md5(jsonObject.toString()));//使用MD5对packet值进行校验，该字段存储MD5摘要值

        return datasUpModel;
    }

}
