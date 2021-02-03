package com.scdy.comprehensiveinsurance.drive.tcp.server;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scdy.comprehensiveinsurance.constant.GlobalConstants;
import com.scdy.comprehensiveinsurance.entity.DictEntity;
import com.scdy.comprehensiveinsurance.entity.EthEntity;
import com.scdy.comprehensiveinsurance.entity.ParamEntity;
import com.scdy.comprehensiveinsurance.entity.SensorEntity;
import com.scdy.comprehensiveinsurance.model.PointModel;
import com.scdy.comprehensiveinsurance.model.SensorModel;
import com.scdy.comprehensiveinsurance.model.request.datasUp.DataUpPacketModel;
import com.scdy.comprehensiveinsurance.model.request.datasUp.DatasUpModel;
import com.scdy.comprehensiveinsurance.model.request.datasUp.DatasUpParamDataModel;
import com.scdy.comprehensiveinsurance.service.*;
import com.scdy.comprehensiveinsurance.service.analysis.impl.EthProtocolAnalysisServiceImpl;
import com.scdy.comprehensiveinsurance.utils.ByteUtil;
import com.scdy.comprehensiveinsurance.utils.DictUtil;
import com.scdy.comprehensiveinsurance.utils.SendDataUtil;
import com.scdy.comprehensiveinsurance.utils.SpringUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
@Data
@ChannelHandler.Sharable
public class TcpServerHandler extends ChannelHandlerAdapter {
    private SensorService sensorService = SpringUtil.getBean(SensorService.class);
    private EthService ethService = SpringUtil.getBean(EthService.class);
    private ParamService paramService = SpringUtil.getBean(ParamService.class);
    private EthProtocolAnalysisServiceImpl protocolAnalysisService = SpringUtil.getBean(EthProtocolAnalysisServiceImpl.class);
    private InfluxdbService influxdbService = SpringUtil.getBean(InfluxdbService.class);
    private MqttGatewayService mqttGatewayService = SpringUtil.getBean(MqttGatewayService.class);
    private ConstructService constructService = SpringUtil.getBean(ConstructService.class);
    private DictUtil dictUtil = SpringUtil.getBean(DictUtil.class);
    private DictService dictService = SpringUtil.getBean(DictService.class);
    private int index = 0;
    private int count = 0;
    private boolean flag = false;


    private EthEntity ethEntity;
    private SensorModel sensorModel;
    private LinkedHashMap<String, String> dataMap = new LinkedHashMap<>();

    public TcpServerHandler(EthEntity ethEntity, SensorModel sensorModel) {
        this.ethEntity = ethEntity;
        this.sensorModel = sensorModel;
    }

    private StringBuffer stringBuffer = new StringBuffer();//用于接收存储串口发过来的数据 必须用static才能接收到到数据 但是可能出现其他问题，多线程字段共享


    /**
     * 客户端接收到数据的回调
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            String str = msg.toString();
            if (StringUtils.isEmpty(str)) {
                return;
            }
            log.info("接收到的指令信息\r\n地址：{}\r\n数据:{}", ctx.channel().remoteAddress(), str);


            if ("5000".equals(ethEntity.getLocalPort())) {//表示蓄电池
                ArrayList<String> strings = ByteUtil.strsToList(str);
                String startWith = strings.get(0) + strings.get(1);
                switch (startWith) {
                    case "0101"://遥信
                    case "0103"://遥测
                        stringBuffer = new StringBuffer();//清空sb
                        break;
                }
            }

            String originalCrc16 = str.substring(str.length() - 4);

            stringBuffer.append(str);
            String sb = stringBuffer.toString();
            String value = sb.substring(0, sb.length() - 4);
            String crc16 = SendDataUtil.getCrc16(value);


            if (!originalCrc16.equals(crc16)) {//crc16不相同 不完整指令
                index++;
                if (index > 20) {
                    stringBuffer = new StringBuffer();//清空sb 避免出现死循环
                    flag = true;
                }
                if (flag) {
                    stringBuffer = new StringBuffer();//清空sb 避免出现死循环
                }
                return;
            } else {//crc16相同 完整指令
                flag = false;
                index = 0;
                str = sb;
                stringBuffer = new StringBuffer();//清空sb
                log.info("最终数据:{}", str);
            }


            String sensorModelId = ethEntity.getSensorModel();
            QueryWrapper<SensorEntity> sensorEntityQueryWrapper1 = new QueryWrapper<>();
            sensorEntityQueryWrapper1.eq("is_deleted", 0);
            sensorEntityQueryWrapper1.eq("sensor_name_code", sensorModelId);
            SensorEntity sensorEntity = sensorService.getOne(sensorEntityQueryWrapper1);


//        //如果是反控必须设置为字节接收信息返回自己处理
//        String isControl = sensorModel.getIsControl();
//        if ("1".equals(isControl)) {
//            sensorEntity.setIsSend("0");
//        }

            switch (sensorEntity.getIsSend()) {
                case "0"://表示接收返回信息自己处理
                    stringBuffer.append(str);
                    break;
                case "1"://表示本类处理
                    ArrayList<String> strList = ByteUtil.strsToList(str);
                    String slaveAddressHex = strList.get(0);//从机地址值16进制
                    Integer slaveAddressDec = ByteUtil.hexStringToNum(slaveAddressHex);//从机地址值10进制
                    String functionCodeHex = strList.get(1);//功能码


                    if ("5000".equals(ethEntity.getLocalPort())) {//表示蓄电池
                        String headValue = (slaveAddressHex + functionCodeHex + strList.get(2) + strList.get(3)).replace(" ", "");//起始地址


                        QueryWrapper<DictEntity> dictEntityQueryWrapper = new QueryWrapper<>();
                        dictEntityQueryWrapper.eq("is_deleted", 0);
                        dictEntityQueryWrapper.eq("type", "battery_request_instructions");
                        dictEntityQueryWrapper.eq("value", headValue);
                        DictEntity dictServiceOne = dictService.getOne(dictEntityQueryWrapper);
                        if (!StringUtils.isEmpty(dictServiceOne)) {//表示请求指令
                            dataMap.put("1", str);
                            return;
                        }


//                        if (count == 0) {//0表示这是请求指令
////
//
////                            if(!StringUtils.isEmpty(dictServiceOne)){
//
//                            count = 1;
////                            }
//                            return;
//                        } else if (count == 1) {//返回指令
//                            count = 0;
//                        }
                    }


                    QueryWrapper<ParamEntity> paramEntityQueryWrapper = new QueryWrapper<>();
                    paramEntityQueryWrapper.eq("is_deleted", 0);
                    paramEntityQueryWrapper.eq("function_code_hex", functionCodeHex);
                    paramEntityQueryWrapper.eq("server_port", ethEntity.getLocalPort());
                    List<ParamEntity> paramEntityList = paramService.list(paramEntityQueryWrapper);
                    String sensorNameCode = null;
                    for (ParamEntity paramEntity : paramEntityList) {
                        sensorNameCode = paramEntity.getSensorNameCode();
                        break;
                    }

                    QueryWrapper<EthEntity> ethEntityQueryWrapper = new QueryWrapper<>();
                    ethEntityQueryWrapper.eq("is_deleted", 0);
                    ethEntityQueryWrapper.eq("sensor_model", sensorNameCode);
                    ethEntityQueryWrapper.eq("device_address", slaveAddressDec);
                    List<EthEntity> ethEntityList = ethService.list(ethEntityQueryWrapper);
                    EthEntity ethEntity = null;
                    for (EthEntity entity : ethEntityList) {//地址不同可以准确找到eth行，地址相同只找第一条eth
                        String deviceAddressDec = entity.getDeviceAddress();//10进制地址值
                        if (slaveAddressDec == Integer.parseInt(deviceAddressDec)) {
                            ethEntity = entity;
                            break;
                        }
                    }

                    QueryWrapper<SensorEntity> sensorEntityQueryWrapper = new QueryWrapper<>();
                    sensorEntityQueryWrapper.eq("is_deleted", 0);
//                    sensorEntityQueryWrapper.eq("type", "eth");
                    sensorEntityQueryWrapper.eq("sensor_name_code", ethEntity.getSensorModel());
                    SensorEntity sensorEntity1 = sensorService.getOne(sensorEntityQueryWrapper);//地址不相同可以找到准确传感器，地址相同找到其中之一传感器


                    this.sensorModel.setIsControl("0");//接收到数据本类处理表示不是反控数据
                    this.sensorModel.setSensorNameId(sensorEntity1.getSensorNameCode());
                    this.sensorModel.setSensorName(sensorEntity1.getSensorName());
                    String deviceAddress = ethEntity.getDeviceAddress();
                    this.sensorModel.setDeviceAddress(deviceAddress);
                    this.sensorModel.setTag(ethEntity.getTag());
                    this.sensorModel.setSensorId(sensorEntity1.getId());


                    //设置返回的指令
                    List<PointModel> pointModelList = this.sensorModel.getPointModelList();
                    pointModelList.clear();//清空集合里面之前解析的
                    PointModel pointModel = new PointModel();
                    pointModel.setRequestInstruction(dataMap.get("1"));
                    dataMap.clear();
                    pointModel.setResponseInstruction(str);
                    pointModelList.add(pointModel);

                    //解析
                    protocolAnalysisService.analysis(this.sensorModel);

                    if ("7".equals(sensorModel.getSensorNameId())) {//传感器是蓄电池
                        HashMap<String, ArrayList<PointModel>> map = new HashMap<>();
                        //构建蓄电池1信息
                        List<PointModel> pointModelList1 = sensorModel.getPointModelList();

                        synchronized (this.ethEntity.getLocalPort()) {
                            for (PointModel model : pointModelList1) {
                                List<PointModel> pointList = model.getPointList();
                                for (PointModel pointModel1 : pointList) {
                                    String ethId = pointModel1.getEthId();
                                    ArrayList<PointModel> pointModels = map.get(ethId);
                                    if (StringUtils.isEmpty(pointModels)) {
                                        ArrayList<PointModel> pointModels1 = new ArrayList<>();
                                        pointModels1.add(pointModel1);
                                        map.put(ethId, pointModels1);
                                    } else {
                                        pointModels.add(pointModel1);
                                    }
                                }
                            }
                        }

                        for (ArrayList<PointModel> pointModels : map.values()) {
                            for (PointModel model : pointModelList1) {
                                model.setPointList(pointModels);

                                String sensorId = null;
                                String ethId = null;
                                for (PointModel pointModel1 : pointModels) {
                                    sensorId = pointModel1.getSensorId();//传感器id
                                    ethId = pointModel1.getEthId();
                                    break;
                                }

                                this.sensorModel.setSensorId(sensorId);
                                this.sensorModel.setEthId(ethId);
                                SensorEntity sensorServiceById = sensorService.getById(sensorId);
                                this.sensorModel.setSensorNameId(sensorServiceById.getSensorNameCode());
                                this.sensorModel.setSensorName(sensorServiceById.getSensorName());
                                EthEntity ethServiceById = ethService.getById(ethId);
                                this.sensorModel.setInstallPostion(ethServiceById.getInstallPostion());
                                this.sensorModel.setTag(ethServiceById.getTag());
                                saveToInfluxDb(this.sensorModel);
                            }
                        }
                    }

                    //风机、烟感、存在探测特殊处理
                    else if ("01".equals(slaveAddressHex) && "02".equals(functionCodeHex)) {
                        SensorModel targetSensorModel = new SensorModel();
                        BeanUtil.copyProperties(this.sensorModel, targetSensorModel);

                        ArrayList<SensorModel> sensorModelList = new ArrayList<>();


                        List<PointModel> pointModelList1 = this.sensorModel.getPointModelList();

//                        int index=0;
                        ArrayList<List<PointModel>> lists = new ArrayList<>();
                        for (PointModel model : pointModelList1) {
                            List<PointModel> pointList = model.getPointList();
//                            HashMap<EthEntity, List<PointModel>> ethEntityListHashMap = new HashMap<>();
                            List<PointModel> fan1List = new ArrayList<>();//风机1
                            fan1List.add(pointList.get(0));
                            fan1List.add(pointList.get(2));
                            lists.add(fan1List);

                            List<PointModel> fan2List = new ArrayList<>();//风机2
                            fan2List.add(pointList.get(1));
                            fan2List.add(pointList.get(3));
                            lists.add(fan2List);

                            List<PointModel> presenceDetction1List = new ArrayList<>();//风机2
                            presenceDetction1List.add(pointList.get(4));
                            lists.add(presenceDetction1List);

                            List<PointModel> presenceDetction2List = new ArrayList<>();//存在探测器2
                            presenceDetction2List.add(pointList.get(6));
                            lists.add(presenceDetction2List);

                            List<PointModel> smokeDetector1List = new ArrayList<>();//烟感1
                            smokeDetector1List.add(pointList.get(5));
                            lists.add(smokeDetector1List);

                            List<PointModel> smokeDetector2List = new ArrayList<>();//烟感2
                            smokeDetector2List.add(pointList.get(7));
                            lists.add(smokeDetector2List);
                        }

                        for (List<PointModel> pointModelList2 : lists) {
                            String sensorId = null;
                            String ethId = null;
                            for (PointModel model : pointModelList2) {
                                sensorId = model.getSensorId();//传感器id
                                ethId = model.getEthId();
                            }
                            targetSensorModel.setSensorId(sensorId);
                            targetSensorModel.setEthId(ethId);
                            SensorEntity sensorServiceById = sensorService.getById(sensorId);
                            targetSensorModel.setSensorNameId(sensorServiceById.getSensorNameCode());
                            targetSensorModel.setSensorName(sensorServiceById.getSensorName());
                            EthEntity ethServiceById = ethService.getById(ethId);
                            targetSensorModel.setInstallPostion(ethServiceById.getInstallPostion());
                            targetSensorModel.setTag(ethServiceById.getTag());
                            List<PointModel> pointModelList3 = targetSensorModel.getPointModelList();
                            for (PointModel model : pointModelList3) {
                                model.setPointName(sensorServiceById.getSensorName());
                                model.setPointList(pointModelList2);
                            }
                            saveToInfluxDb(targetSensorModel);
                        }


                    } else {
                        List<PointModel> pointModelList1 = this.sensorModel.getPointModelList();
                        String sensorId = null;
                        String ethId = null;
                        for (PointModel model : pointModelList1) {
                            List<PointModel> pointList = model.getPointList();
                            for (PointModel pointModel1 : pointList) {
                                sensorId = pointModel1.getSensorId();//传感器id
                                ethId = pointModel1.getEthId();
                                break;
                            }
                        }


                        this.sensorModel.setSensorId(sensorId);
                        this.sensorModel.setEthId(ethId);
                        SensorEntity sensorServiceById = sensorService.getById(sensorId);
                        this.sensorModel.setSensorNameId(sensorServiceById.getSensorNameCode());
                        this.sensorModel.setSensorName(sensorServiceById.getSensorName());
                        EthEntity ethServiceById = ethService.getById(ethId);
                        this.sensorModel.setInstallPostion(ethServiceById.getInstallPostion());
                        this.sensorModel.setTag(ethServiceById.getTag());
                        List<PointModel> pointModelList3 = this.sensorModel.getPointModelList();
                        for (PointModel model : pointModelList3) {
                            model.setPointName(sensorServiceById.getSensorName());
                        }
                        saveToInfluxDb(this.sensorModel);
                    }
                    break;
                case "2"://表示返回去自己处理，同时还要不定时接收信息
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveToInfluxDb(SensorModel sensorModel) throws Exception {
        //4、保持到时序数据库


        influxdbService.saveEthToInfluxdb(sensorModel);


        boolean flag = true;
        List<PointModel> pointModelList1 = sensorModel.getPointModelList();
        for (PointModel model : pointModelList1) {
            String responseInstruction = model.getResponseInstruction();
            if (StringUtils.isEmpty(responseInstruction)) {
                flag = false;
            }
        }

        if (flag) {

            DatasUpModel datasUp = constructService.getDatasUp(sensorModel);

            String data = JSONUtil.parseObj(datasUp, false, true).toString();

            String topic = "/v1/" + dictUtil.getDictValue("mqtt", "server_id") + "/devices/datas/up";
            mqttGatewayService.sendToMqtt(data, topic);
            log.info("发送到mqtt\r\n名称:{}\r\n主题:{}\r\n数据：{}", "数据上传", topic, data);


            //更新设备时间，用于设备是否在线监测
            JSONObject packet1 = datasUp.getPacket();
            DataUpPacketModel packet = JSONUtil.toBean(packet1, DataUpPacketModel.class);
//            DataUpPacketModel packet = datasUp.getPacket();
            List<DatasUpParamDataModel> datas = packet.getDatas();
            for (DatasUpParamDataModel datasUpParamDataModel : datas) {
                String deviceId = datasUpParamDataModel.getDeviceId();
                EthEntity ethEntity = ethService.getById(deviceId);
                ethEntity.setUpdateTime(DateUtil.toLocalDateTime(DateUtil.dateSecond()));
                ethService.updateById(ethEntity);
            }
        }
    }


    /**
     * 获取串口发过来的完整数据，并且清空stringBuffer内容
     *
     * @return
     */
    public String receiveMsg() throws InterruptedException {
        Thread.sleep(1000);//延迟1秒接收获取到的指令
        String str = stringBuffer.toString();
        stringBuffer = new StringBuffer();
        return str;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        Map<String, ChannelHandlerContext> channelHandlerContextMap = GlobalConstants.getTcpClientChannelMap();
        channelHandlerContextMap.put(ctx.channel().remoteAddress().toString(), ctx);
        log.info("客户端连接信息:{}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        Map<String, ChannelHandlerContext> channelHandlerContextMap = GlobalConstants.getTcpClientChannelMap();
        channelHandlerContextMap.remove(ctx.channel().remoteAddress().toString());
        stringBuffer = new StringBuffer();//清空sb 避免出现死循环
        log.info("客户端断开信息:{}", ctx.channel().remoteAddress());
    }
}
