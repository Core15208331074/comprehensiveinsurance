package com.scdy.comprehensiveinsurance.service.analysis.impl;
/**
 * 串口协议解析.
 */

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scdy.comprehensiveinsurance.constant.GlobalConstants;
import com.scdy.comprehensiveinsurance.entity.*;
import com.scdy.comprehensiveinsurance.model.PointModel;
import com.scdy.comprehensiveinsurance.model.SensorModel;
import com.scdy.comprehensiveinsurance.service.*;
import com.scdy.comprehensiveinsurance.service.analysis.ProtocolAnalysisService;
import com.scdy.comprehensiveinsurance.utils.ByteUtil;
import com.scdy.comprehensiveinsurance.utils.DictUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Service
@Transactional
public class EthProtocolAnalysisServiceImpl implements ProtocolAnalysisService {
    @Autowired
    private DictService dictService;
    @Autowired
    private DictUtil dictUtil;
    @Autowired
    private ParamService paramService;
    @Autowired
    private EthService ethService;
    @Autowired
    private SensorService sensorService;
    @Autowired
    private DeviceParamService deviceParamService;

    @Override
    public SensorModel analysis(SensorModel sensorModel) throws Exception {
//        List<PointModel> pointModelList = sensorModel.getPointModelList();
        String sensorNameId = sensorModel.getSensorNameId();
//        if (StringUtils.isEmpty(sensorNameId)) {
//            return sensorModel;//表示反控，不解析
//        }
//        for (PointModel pointModel : pointModelList) {
//        }

        switch (sensorNameId) {
            case "4"://电子围栏
                electronicFenceAnalysis(sensorModel);
                break;
            case "2"://模拟假传感器 火灾传感器
                protocolAnalysis(sensorModel);
                break;
            case "6"://环控(利通)
            case "8"://温湿度(利通环控)
            case "9"://照明(利通环控)
            case "10"://风机(利通环控)
            case "11"://烟感(利通环控)
            case "12"://存在探测(利通环控)
            case "13"://空调(利通环控)
                envControlAnalysis(sensorModel);
                break;
            case "7"://蓄电池(利通)
                batteryLitongAnalysis(sensorModel);
                break;
            case "14"://峨山蓄电池
                batteryMountEmeiAnalysis(sensorModel);
                break;
        }


        return sensorModel;
    }


    /**
     * 峨山蓄电池
     *
     * @param sensorModel
     */
    private void batteryMountEmeiAnalysis(SensorModel sensorModel) {
        sensorModel.setType(GlobalConstants.TCP_CLIENT);
        List<PointModel> pointModelList = sensorModel.getPointModelList();
        for (PointModel pointModel : pointModelList) {
            String pointName = pointModel.getPointName();
            switch (pointName) {
                case GlobalConstants.BATTERY_MOUNT_E_SINGLE_VOLTAGE_1_100://单体电压1-100
                case GlobalConstants.BATTERY_MOUNT_E_SINGLE_VOLTAGE_101_200://单体电压101-200
                case GlobalConstants.BATTERY_MOUNT_E_SINGLE_VOLTAGE_201_300://单体电压201-300

                case GlobalConstants.BATTERY_MOUNT_E_MONOMER_RESISTANCE_1_100://单体内阻1-100
                case GlobalConstants.BATTERY_MOUNT_E_MONOMER_RESISTANCE_101_200://单体内阻101-200
                case GlobalConstants.BATTERY_MOUNT_E_MONOMER_RESISTANCE_201_300://单体内阻201-300

                case GlobalConstants.BATTERY_MOUNT_E_BATTERY_TEMPERATURE_1_100://电池温度1-100
                case GlobalConstants.BATTERY_MOUNT_E_BATTERY_TEMPERATURE_101_200://电池温度101-200
                case GlobalConstants.BATTERY_MOUNT_E_BATTERY_TEMPERATURE_201_300://电池温度101-200

                case GlobalConstants.BATTERY_MOUNT_E_GROUP_PRESSURE://电池组总电压


                    //获取起始地址
                    //11 03 00 03 00 64 B6 B1
                    String requestInstruction = pointModel.getRequestInstruction().replace(" ", "");
                    if (StringUtils.isEmpty(requestInstruction)) {
                        return;
                    }
                    ArrayList<String> requestInstructionList = ByteUtil.strsToList(requestInstruction);
                    String startAddressHex = requestInstructionList.get(2) + requestInstructionList.get(3);
                    pointModel.setDataStartAddress(startAddressHex);

                    //获取返回指令的list
                    String responseInstruction = pointModel.getResponseInstruction().replace(" ", "");
                    if (StringUtils.isEmpty(responseInstruction)) {
                        return;
                    }
                    ArrayList<String> responseInstructionList = ByteUtil.strsToList(responseInstruction);

                    //设置从机地址
                    pointModel.setSlaveAddressHex(responseInstructionList.get(0));

                    //设置功能码
                    pointModel.setFunctionCodeHex(responseInstructionList.get(1));

                    //设置数据长度
                    String totalDataBytesHex = responseInstructionList.get(2);
                    pointModel.setTotalDataBytesHex(totalDataBytesHex);


                    //解析point
                    ArrayList<PointModel> pointList = new ArrayList<>();

                    //获取数据长度10进制
                    int totalDataBytesDec = ByteUtil.hexStringToNum(totalDataBytesHex);
                    double valueNum = NumberUtil.div(totalDataBytesDec, 2);
                    int index = 3;//从3开始才是真正的值
                    for (int i = 0; i < valueNum; i++) {
                        PointModel modelPoint = new PointModel();
                        int tag = 0;
                        switch (pointName) {
                            case GlobalConstants.BATTERY_MOUNT_E_SINGLE_VOLTAGE_1_100://单体电压1-100
                            case GlobalConstants.BATTERY_MOUNT_E_MONOMER_RESISTANCE_1_100://单体内阻1-100
                            case GlobalConstants.BATTERY_MOUNT_E_BATTERY_TEMPERATURE_1_100://电池温度1-100
                                tag = i + 1;
                                break;
                            case GlobalConstants.BATTERY_MOUNT_E_SINGLE_VOLTAGE_101_200://单体电压101-200
                            case GlobalConstants.BATTERY_MOUNT_E_MONOMER_RESISTANCE_101_200://单体内阻101-200
                            case GlobalConstants.BATTERY_MOUNT_E_BATTERY_TEMPERATURE_101_200://电池温度101-200
                                tag = i + 101;
                                break;
                            case GlobalConstants.BATTERY_MOUNT_E_SINGLE_VOLTAGE_201_300://单体电压201-300
                            case GlobalConstants.BATTERY_MOUNT_E_MONOMER_RESISTANCE_201_300://单体内阻201-300
                            case GlobalConstants.BATTERY_MOUNT_E_BATTERY_TEMPERATURE_201_300://电池温度101-200
                                tag = i + 201;
                                break;
                            case GlobalConstants.BATTERY_MOUNT_E_GROUP_PRESSURE://电池组总电压
                                tag = i + 1;
                                break;
                        }
                        StringBuilder tempValue = new StringBuilder();
                        for (int j = 0; j < 2; j++) {
                            tempValue.append(responseInstructionList.get(index));
                            index++;
                        }
                        //得到16进制的值
                        String valueHex = tempValue.toString();

                        QueryWrapper<ParamEntity> paramEntityQueryWrapper = new QueryWrapper<>();
                        switch (pointName) {
                            case GlobalConstants.BATTERY_MOUNT_E_SINGLE_VOLTAGE_1_100://单体电压1-100
                            case GlobalConstants.BATTERY_MOUNT_E_SINGLE_VOLTAGE_101_200://单体电压101-200
                            case GlobalConstants.BATTERY_MOUNT_E_SINGLE_VOLTAGE_201_300://单体电压201-300
                                //设置名字
                                modelPoint.setPointName("单体电压");

                                //设置单位
                                modelPoint.setUnit("V");

                                //设置转换后的值
                                int valueDec = ByteUtil.hexStringToNum(valueHex);
                                modelPoint.setValue(NumberUtil.mul(valueDec, 0.001) + "");

                                paramEntityQueryWrapper.eq("parameter", "单体电压");
                                break;
                            case GlobalConstants.BATTERY_MOUNT_E_MONOMER_RESISTANCE_1_100://单体内阻1-100
                            case GlobalConstants.BATTERY_MOUNT_E_MONOMER_RESISTANCE_101_200://单体内阻101-200
                            case GlobalConstants.BATTERY_MOUNT_E_MONOMER_RESISTANCE_201_300://单体内阻201-300
                                //设置名字
                                modelPoint.setPointName("单体内阻");

                                //设置单位
                                modelPoint.setUnit("μΩ");

                                //设置转换后的值
                                modelPoint.setValue(ByteUtil.hexStringToNum(valueHex) + "");

                                paramEntityQueryWrapper.eq("parameter", "单体内阻");
                                break;
                            case GlobalConstants.BATTERY_MOUNT_E_BATTERY_TEMPERATURE_1_100://电池温度1-100
                            case GlobalConstants.BATTERY_MOUNT_E_BATTERY_TEMPERATURE_101_200://电池温度101-200
                            case GlobalConstants.BATTERY_MOUNT_E_BATTERY_TEMPERATURE_201_300://电池温度101-200
                                //设置名字
                                modelPoint.setPointName("电池温度");

                                //设置单位
                                modelPoint.setUnit("℃");

                                //设置转换后的值
                                int batteryTemperatureValueDec = ByteUtil.hexStringToNum(valueHex);
                                modelPoint.setValue(NumberUtil.mul(batteryTemperatureValueDec, 0.1) + "");

                                paramEntityQueryWrapper.eq("parameter", "电池温度");
                                break;
                            case GlobalConstants.BATTERY_MOUNT_E_GROUP_PRESSURE://电池组总电压
                                //设置名字
                                modelPoint.setPointName("电池组总电压");

                                //设置单位
                                modelPoint.setUnit("V");

                                //设置转换后的值
                                int groupPressureValueDec = ByteUtil.hexStringToNum(valueHex);
                                modelPoint.setValue(NumberUtil.mul(groupPressureValueDec, 0.1) + "");

                                paramEntityQueryWrapper.eq("parameter", "电池组总电压");
                                break;
                        }


                        //设置tag
                        modelPoint.setTag(tag + "");

                        //设置原始值
                        modelPoint.setOriginalValue(valueHex);


                        //设置数据地址
                        Integer startAddressDec = ByteUtil.hexStringToNum(startAddressHex);
                        int dataAddressDec = startAddressDec + i;
                        modelPoint.setDataAddress(ByteUtil.numToLengthHexStr(dataAddressDec, 4));

                        //设置sensorId
                        modelPoint.setSensorId(sensorModel.getSensorId());

                        //设置deviceId
                        modelPoint.setEthId(sensorModel.getEthId());

                        //设置参数Id(deviceParamId)

                        paramEntityQueryWrapper.eq("is_deleted", 0);
                        paramEntityQueryWrapper.eq("sensor_name_code", sensorModel.getSensorNameId());
                        paramEntityQueryWrapper.eq("tag", tag);
                        ParamEntity paramEntity = paramService.getOne(paramEntityQueryWrapper);
                        String paramId = paramEntity.getId();//参数id
                        String deviceId = sensorModel.getEthId();//设备id

                        QueryWrapper<DeviceParamEntity> deviceParamEntityQueryWrapper = new QueryWrapper<>();
                        deviceParamEntityQueryWrapper.eq("is_deleted", 0);
                        deviceParamEntityQueryWrapper.eq("device_id", deviceId);
                        deviceParamEntityQueryWrapper.eq("param_id", paramId);
                        DeviceParamEntity deviceParamEntity = deviceParamService.getOne(deviceParamEntityQueryWrapper);
                        modelPoint.setParamId(deviceParamEntity.getId());

                        pointList.add(modelPoint);
                    }

                    //添加point
                    pointModel.setPointList(pointList);

                    //设置时间
                    pointModel.setTime(DateUtil.now());

                    //设置CRC校验值
                    pointModel.setCRCHex(responseInstructionList.get(responseInstructionList.size() - 2) + responseInstructionList.get(responseInstructionList.size() - 1));
                    break;

            }

        }
    }

    /**
     * 蓄电池(利通)
     *
     * @param sensorModel
     */
    private void batteryLitongAnalysis(SensorModel sensorModel) {
        sensorModel.setType(GlobalConstants.TCP_SERVER);
        List<PointModel> pointModelList = sensorModel.getPointModelList();
        for (PointModel pointModel : pointModelList) {
            //请求指令 如：01 03 00 00 00 10 44 06
            String requestInstruction = pointModel.getRequestInstruction().replace(" ", "");
            if (StringUtils.isEmpty(requestInstruction)) {
                return;
            }
            //1、获取返回指令去空
            String responseInstruction = pointModel.getResponseInstruction().replace(" ", "");
            if (StringUtils.isEmpty(responseInstruction)) {
                return;
            }

            ArrayList<String> requestInstructionList = ByteUtil.strsToList(requestInstruction);
            String startAddressHex = requestInstructionList.get(2) + requestInstructionList.get(3);//起始地址

            //2、全部分成2个字符为一组
            ArrayList<String> responseInstructionList = ByteUtil.strsToList(responseInstruction);
            String deviceAddressHex = responseInstructionList.get(0);//设备地址
            pointModel.setSlaveAddressHex(deviceAddressHex);

            String functionCodeHex = responseInstructionList.get(1);//功能码
            pointModel.setFunctionCodeHex(functionCodeHex);

            if ("03".equals(functionCodeHex)) {//功能码为03 遥测
                switch (startAddressHex) {
                    case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_1://0000
                    case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_2://0028
                    case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_3://03E8
                    case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_4://0410
                    case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_5://0438
                    case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_6://0460
                    case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_7://0488

                        switch (startAddressHex) {
                            case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_1://0000
                                pointModel.setPointName(GlobalConstants.BATTERY_LITONG_TELEMETERING_0_39);//遥测0_39
                                break;
                            case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_2://0028
                                pointModel.setPointName(GlobalConstants.BATTERY_LITONG_TELEMETERING_40_79);//遥测40_79
                                break;
                            case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_3://03E8
                                pointModel.setPointName(GlobalConstants.BATTERY_LITONG_TELEMETERING_1000_1039);//遥测1000_1039
                                break;
                            case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_4://0410
                                pointModel.setPointName(GlobalConstants.BATTERY_LITONG_TELEMETERING_1040_1079);//遥测1040_1079
                                break;
                            case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_5://0438
                                pointModel.setPointName(GlobalConstants.BATTERY_LITONG_TELEMETERING_1080_1119);//遥测1080_1119
                                break;
                            case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_6://0460
                                pointModel.setPointName(GlobalConstants.BATTERY_LITONG_TELEMETERING_1120_1159);//遥测1120_1159
                                break;
                            case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_7://0488
                                pointModel.setPointName(GlobalConstants.BATTERY_LITONG_TELEMETERING_1160_1207);//遥测1160_1207
                                break;
                        }


                        pointModel.setDataStartAddress(startAddressHex);


                        //返回指令 如：01 03 20 55 F0 56 54 56 B8 03 E8 04 4C 04 B0 03 E8 04 4C 04 B0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 51 F3
                        String dataLengthHex = responseInstructionList.get(2);//数据长度
                        pointModel.setTotalDataBytesHex(dataLengthHex);//数据长度
                        pointModel.setCRCHex(responseInstructionList.get(responseInstructionList.size() - 2) + responseInstructionList.get(responseInstructionList.size() - 1));


                        ArrayList<PointModel> pointList = new ArrayList<>();

                        //解析16进制data
                        LinkedHashMap<String, String> dataMapHex = new LinkedHashMap<>();

                        //开始解析
                        int index = 3;//设置从3开始获取
                        Integer dataLengthDec = ByteUtil.hexStringToNum(dataLengthHex);

                        Integer startAddressDec = ByteUtil.hexStringToNum(startAddressHex);//起始地址10进制
                        for (int i = 0; i < dataLengthDec / 2; i++) {//如：循环16次
                            StringBuilder valueHexTemp = new StringBuilder();
                            for (int j = 0; j < 2; j++) {//循环2次
                                valueHexTemp.append(responseInstructionList.get(index));
                                index++;
                            }

                            String key = deviceAddressHex + "_" + functionCodeHex + "_" + startAddressDec;
                            QueryWrapper<DictEntity> dictEntityQueryWrapper1 = new QueryWrapper<>();
                            dictEntityQueryWrapper1.eq("is_deleted", 0);
                            dictEntityQueryWrapper1.eq("type", "battery_ethId_paramId");
                            dictEntityQueryWrapper1.eq("name", key);
                            DictEntity dictServiceOne = dictService.getOne(dictEntityQueryWrapper1);

                            Integer startAddressDec1 = startAddressDec;
                            startAddressDec++;
                            if (StringUtils.isEmpty(dictServiceOne)) {
                                continue;
                            }

                            String[] split = dictServiceOne.getValue().split("_");
                            String ethId = split[0];//eth行id
                            String paramId = split[1];//真参数id
                            ParamEntity paramServiceById = paramService.getById(paramId);


                            EthEntity ethServiceById = ethService.getById(ethId);

                            QueryWrapper<SensorEntity> sensorEntityQueryWrapper = new QueryWrapper<>();
                            sensorEntityQueryWrapper.eq("is_deleted", 0);
                            sensorEntityQueryWrapper.eq("sensor_name_code", ethServiceById.getSensorModel());
                            SensorEntity sensorServiceOne = sensorService.getOne(sensorEntityQueryWrapper);

                            QueryWrapper<DeviceParamEntity> deviceParamEntityQueryWrapper = new QueryWrapper<>();
                            deviceParamEntityQueryWrapper.eq("is_deleted", 0);
                            deviceParamEntityQueryWrapper.eq("device_id", ethId);
                            deviceParamEntityQueryWrapper.eq("param_id", paramId);
                            DeviceParamEntity deviceParamServiceOne = deviceParamService.getOne(deviceParamEntityQueryWrapper);
                            String id = deviceParamServiceOne.getId();//作为参数伪id


                            //解析
                            String valueHex = valueHexTemp.toString();
                            dataMapHex.put(startAddressDec1 + "", valueHex);

                            //解析规则
                            Integer valueDec = ByteUtil.hexStringToNum(valueHex);
                            double value = 0;

                            PointModel modelPoint = new PointModel();

                            modelPoint.setEthId(ethId);
                            modelPoint.setSensorId(sensorServiceOne.getId());
                            modelPoint.setParamId(id);

//                            QueryWrapper<DictEntity> dictEntityQueryWrapper = new QueryWrapper<>();
//                            dictEntityQueryWrapper.eq("is_deleted", 0);
                            int valueDec1 = valueDec;
                            switch (startAddressHex) {
                                case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_1:
                                case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_2:
//                                    value = valueDec * 0.1;
                                    value = NumberUtil.mul(valueDec1, 0.1);
//                                    dictEntityQueryWrapper.eq("type", "battery_litong_telemetering_0_79");
//                                    dictEntityQueryWrapper.eq("name", "yc_03_" + i);
                                    break;
                                case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_3:
                                case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_4:
                                case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_5:
                                case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_6:
                                case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_7:
//                                    value = valueDec * 0.001;

                                    value = NumberUtil.mul(valueDec1, 0.001);
//                                    dictEntityQueryWrapper.eq("type", "battery_litong_telemetering_1000_1207");
//                                    dictEntityQueryWrapper.eq("name", "yc_03_" + (1000 + i));
                                    break;

                            }

//                            DictEntity dictEntity = dictService.getOne(dictEntityQueryWrapper);
//                            if (!StringUtils.isEmpty(dictEntity)) {
//                                String value1 = dictEntity.getValue();
//                                modelPoint.setUnit(value1);//单位
//                            }
                            modelPoint.setUnit(paramServiceById.getUnit());//单位
                            modelPoint.setTag(ethServiceById.getTag() + "_" + paramServiceById.getTag());
                            modelPoint.setPointName(paramServiceById.getParameter());

                            modelPoint.setValue(value + "");

                            modelPoint.setDataAddress(ByteUtil.numToLengthHexStr(startAddressDec1, 4));


                            pointList.add(modelPoint);
                        }


                        pointModel.setDataMapHex(dataMapHex);
                        pointModel.setPointList(pointList);//添加子point


                        pointModel.setTime(DateUtil.now());//设置时间

                        break;

                }


            } else if ("01".equals(functionCodeHex)) {//功能码为01 遥信
                switch (startAddressHex) {
                    case GlobalConstants.BATTERY_LITONG_TELEMETERING_STARTADDRESS_1://0000
                        pointModel.setPointName(GlobalConstants.BATTERY_LITONG_TELEMETERING_144);//遥信144
                        //请求指令 如：01 01 00 00 00 10 3D C6
                        String telecommunicationStartAddressHex = requestInstructionList.get(2) + requestInstructionList.get(3);//起始地址
                        pointModel.setDataStartAddress(telecommunicationStartAddressHex);


                        //返回指令 如：01 01 02 00 00 B9 FC
//                        pointModel.setSlaveAddressHex(responseInstructionList.get(0));//从机地址
//                        pointModel.setFunctionCodeHex(responseInstructionList.get(1));//功能码
                        String telecommunicationDataLengthHex = responseInstructionList.get(2);//数据长度
                        pointModel.setTotalDataBytesHex(telecommunicationDataLengthHex);//数据长度
                        pointModel.setCRCHex(responseInstructionList.get(responseInstructionList.size() - 2) + responseInstructionList.get(responseInstructionList.size() - 1));


                        ArrayList<PointModel> telecommunicationPointList = new ArrayList<>();
                        LinkedHashMap<String, String> telecommunicationDataMapHex = new LinkedHashMap<>();

                        //开始解析
                        Integer telecommunicationDataLengthDec = ByteUtil.hexStringToNum(telecommunicationDataLengthHex);

                        Integer telecommunicationStartAddressDec = ByteUtil.hexStringToNum(telecommunicationStartAddressHex);//起始地址10进制

                        ArrayList<Integer> valueList = new ArrayList<>();

                        int telecommunicationIndex = 3;//设置从3开始获取
                        for (int i = 0; i < telecommunicationDataLengthDec; i++) {//如：循环2次
                            String valueHex = responseInstructionList.get(telecommunicationIndex);
                            telecommunicationDataMapHex.put((i + 1) + "", valueHex);
                            telecommunicationIndex++;
                            int[] ints = ByteUtil.hexToBinToInts(valueHex);
                            CollectionUtil.addAll(valueList, ints);
                        }

                        for (int i = 0; i < valueList.size(); i++) {

                            String key = deviceAddressHex + "_" + functionCodeHex + "_" + i;
                            QueryWrapper<DictEntity> dictEntityQueryWrapper1 = new QueryWrapper<>();
                            dictEntityQueryWrapper1.eq("is_deleted", 0);
                            dictEntityQueryWrapper1.eq("type", "battery_ethId_paramId");
                            dictEntityQueryWrapper1.eq("name", key);
                            DictEntity dictServiceOne = dictService.getOne(dictEntityQueryWrapper1);

                            if (StringUtils.isEmpty(dictServiceOne)) {
                                continue;
                            }

                            String[] split = dictServiceOne.getValue().split("_");
                            String ethId = split[0];//eth行id
                            String paramId = split[1];//真参数id
                            ParamEntity paramServiceById = paramService.getById(paramId);


                            EthEntity ethServiceById = ethService.getById(ethId);

                            QueryWrapper<SensorEntity> sensorEntityQueryWrapper = new QueryWrapper<>();
                            sensorEntityQueryWrapper.eq("is_deleted", 0);
                            sensorEntityQueryWrapper.eq("sensor_name_code", ethServiceById.getSensorModel());
                            SensorEntity sensorServiceOne = sensorService.getOne(sensorEntityQueryWrapper);

                            QueryWrapper<DeviceParamEntity> deviceParamEntityQueryWrapper = new QueryWrapper<>();
                            deviceParamEntityQueryWrapper.eq("is_deleted", 0);
                            deviceParamEntityQueryWrapper.eq("device_id", ethId);
                            deviceParamEntityQueryWrapper.eq("param_id", paramId);
                            DeviceParamEntity deviceParamServiceOne = deviceParamService.getOne(deviceParamEntityQueryWrapper);
                            String id = deviceParamServiceOne.getId();//作为参数伪id


                            Integer value = valueList.get(i);//结果


                            PointModel modelPoint = new PointModel();


                            modelPoint.setEthId(ethId);
                            modelPoint.setSensorId(sensorServiceOne.getId());
                            modelPoint.setParamId(id);

//                            QueryWrapper<DictEntity> dictEntityQueryWrapper = new QueryWrapper<>();
//                            dictEntityQueryWrapper.eq("is_deleted", 0);
//                            dictEntityQueryWrapper.eq("type", "battery_litong_telecommunication");
//                            dictEntityQueryWrapper.eq("name", "yx_01_" + i);
//                            DictEntity dictEntity = dictService.getOne(dictEntityQueryWrapper);


                            String value1 = paramServiceById.getValue();
                            if (!StringUtils.isEmpty(value1)) {
                                HashMap<Integer, String> map = new HashMap<>();
//                                String value1 = dictEntity.getValue();//如 0:正常,1:告警
                                if (!StringUtils.isEmpty(value1)) {
                                    String[] strings = value1.split(",");//0:正常 1:告警
                                    String s1 = strings[0];//0:正常
                                    String[] split1 = s1.split(":");//0 正常
                                    map.put(Integer.parseInt(split1[0]), split1[1]);

                                    String s2 = strings[1];//1:告警
                                    String[] split2 = s2.split(":");//1 告警
                                    map.put(Integer.parseInt(split2[0]), split2[1]);
                                }
                                modelPoint.setValue(map.get(value));


                            }
                            modelPoint.setTag(ethServiceById.getTag() + "_" + paramServiceById.getTag());
                            modelPoint.setPointName(paramServiceById.getParameter());

                            modelPoint.setDataAddress(ByteUtil.numToLengthHexStr(telecommunicationStartAddressDec, 4));
                            telecommunicationStartAddressDec++;
                            telecommunicationPointList.add(modelPoint);
                        }
                        pointModel.setDataMapHex(telecommunicationDataMapHex);
                        pointModel.setPointList(telecommunicationPointList);//添加子point
                        pointModel.setTime(DateUtil.now());//设置时间
                        break;
                }
            }
        }
    }


    /**
     * 环控(利通)
     *
     * @param sensorModel
     */
    private void envControlAnalysis(SensorModel sensorModel) {
        sensorModel.setType(GlobalConstants.TCP_SERVER);
        List<PointModel> pointModelList = sensorModel.getPointModelList();
        for (PointModel pointModel : pointModelList) {
            //1、获取返回指令去空
            String responseInstruction = pointModel.getResponseInstruction().replace(" ", "");
            if (StringUtils.isEmpty(responseInstruction)) {
                return;
            }
            //2、全部分成2个字符为一组
            ArrayList<String> responseInstructionList = ByteUtil.strsToList(responseInstruction);
            String deviceAddressHex = responseInstructionList.get(0);//设备地址
            pointModel.setSlaveAddressHex(deviceAddressHex);

            String functionCodeHex = responseInstructionList.get(1);//功能码
            pointModel.setFunctionCodeHex(functionCodeHex);


            switch (functionCodeHex) {
                case GlobalConstants.ENVIRONMENTAL_CONTROL_LITONG_TEMPERATURE_HUMIDITY_FUNCTION_CODE://04
//                    QueryWrapper<DictEntity> dictEntityQueryWrapper = new QueryWrapper<>();
//                    dictEntityQueryWrapper.eq("is_deleted", 0);
//                    dictEntityQueryWrapper.eq("type", "temperature_humidity");
//                    dictEntityQueryWrapper.eq("name", deviceAddressHex + "_" + functionCodeHex);
//                    DictEntity dictEntity = dictService.getOne(dictEntityQueryWrapper);

                    pointModel.setPointName(GlobalConstants.ENVIRONMENTAL_CONTROL_LITONG_TEMPERATURE_HUMIDITY);//温湿度

                    String lengthHex = responseInstructionList.get(2);//长度
                    pointModel.setTotalDataBytesHex(lengthHex);

                    ArrayList<PointModel> pointList = new ArrayList<>();
                    LinkedHashMap<String, String> dataMapHex = new LinkedHashMap<>();

                    //温度
                    String temperatureHex = responseInstructionList.get(3) + responseInstructionList.get(4);//温度（寄存器地址：0000）
                    dataMapHex.put("1", temperatureHex);

                    PointModel temperatureModelPoint = new PointModel();

                    String value = dictUtil.getDictValue("temperature_humidity", "3_4");
                    String[] split1 = value.split("_");
                    String sensorId = split1[0];//传感器id
                    String paramId = split1[1];//参数id

                    SensorEntity sensorServiceById = sensorService.getById(sensorId);

                    QueryWrapper<EthEntity> ethEntityQueryWrapper1 = new QueryWrapper<>();
                    ethEntityQueryWrapper1.eq("is_deleted", 0);
                    ethEntityQueryWrapper1.eq("sensor_model", sensorServiceById.getSensorNameCode());
                    switch (deviceAddressHex) {
                        case "02":
                            ethEntityQueryWrapper1.eq("tag", 1);//表示温湿度传感器eth行的tag
                            break;
                        case "03":
                            ethEntityQueryWrapper1.eq("tag", 2);
                            break;
                        case "04":
                            ethEntityQueryWrapper1.eq("tag", 3);
                            break;
                        case "05":
                            ethEntityQueryWrapper1.eq("tag", 4);
                            break;
                    }
                    EthEntity ethEntity = ethService.getOne(ethEntityQueryWrapper1);
                    ParamEntity paramServiceById = paramService.getById(paramId);

                    temperatureModelPoint.setPointName(paramServiceById.getObjType());
                    int temperatureDec = ByteUtil.hexStringToNum(temperatureHex);
                    double temperature = NumberUtil.mul(temperatureDec, 0.01);
                    temperatureModelPoint.setValue(temperature + "");
                    temperatureModelPoint.setUnit(paramServiceById.getUnit());
                    temperatureModelPoint.setDataAddress("0000");
                    temperatureModelPoint.setSensorId(sensorId);

                    QueryWrapper<DeviceParamEntity> deviceParamEntityQueryWrapper = new QueryWrapper<>();
                    deviceParamEntityQueryWrapper.eq("is_deleted", 0);
                    deviceParamEntityQueryWrapper.eq("device_id", ethEntity.getId());
                    deviceParamEntityQueryWrapper.eq("param_id", paramId);
                    DeviceParamEntity deviceParamServiceOne = deviceParamService.getOne(deviceParamEntityQueryWrapper);

                    temperatureModelPoint.setParamId(deviceParamServiceOne.getId());
                    temperatureModelPoint.setEthId(ethEntity.getId());
                    temperatureModelPoint.setTag(ethEntity.getTag() + "_" + paramServiceById.getTag());

                    pointList.add(temperatureModelPoint);

                    //湿度
                    String humidityHex = responseInstructionList.get(5) + responseInstructionList.get(6);//湿度（寄存器地址：0001)
                    dataMapHex.put("2", humidityHex);

                    String value1 = dictUtil.getDictValue("temperature_humidity", "5_6");
                    String[] split2 = value1.split("_");
                    String sensorId2 = split2[0];//传感器id
                    String paramId2 = split2[1];//参数id

                    QueryWrapper<EthEntity> ethEntityQueryWrapper2 = new QueryWrapper<>();
                    ethEntityQueryWrapper2.eq("is_deleted", 0);
                    ethEntityQueryWrapper2.eq("sensor_model", sensorServiceById.getSensorNameCode());
                    switch (deviceAddressHex) {
                        case "02":
                            ethEntityQueryWrapper2.eq("tag", 1);//表示温湿度传感器eth行的tag
                            break;
                        case "03":
                            ethEntityQueryWrapper2.eq("tag", 2);
                            break;
                        case "04":
                            ethEntityQueryWrapper2.eq("tag", 3);
                            break;
                        case "05":
                            ethEntityQueryWrapper2.eq("tag", 4);
                            break;
                    }

                    EthEntity ethEntity2 = ethService.getOne(ethEntityQueryWrapper2);

                    ParamEntity paramServiceById1 = paramService.getById(paramId2);
                    PointModel humidityModelPoint = new PointModel();
                    humidityModelPoint.setPointName(paramServiceById1.getObjType());
                    int humiditydec = ByteUtil.hexStringToNum(humidityHex);
                    double humidity = NumberUtil.mul(humiditydec, 0.01);
                    humidityModelPoint.setValue(humidity + "");
                    humidityModelPoint.setUnit(paramServiceById1.getUnit());
                    humidityModelPoint.setDataAddress("0001");
                    humidityModelPoint.setSensorId(sensorId2);

                    QueryWrapper<DeviceParamEntity> humidityQueryWrapper = new QueryWrapper<>();
                    humidityQueryWrapper.eq("is_deleted", 0);
                    humidityQueryWrapper.eq("device_id", ethEntity2.getId());
                    humidityQueryWrapper.eq("param_id", paramId2);
                    DeviceParamEntity humidityDeviceParam = deviceParamService.getOne(humidityQueryWrapper);

                    humidityModelPoint.setParamId(humidityDeviceParam.getId());
                    humidityModelPoint.setEthId(ethEntity2.getId());
                    humidityModelPoint.setTag(ethEntity2.getTag() + "_" + paramServiceById1.getTag());
                    pointList.add(humidityModelPoint);

                    pointModel.setDataMapHex(dataMapHex);
                    pointModel.setPointList(pointList);//添加子point


                    pointModel.setTime(DateUtil.now());//设置时间
                    break;
                case GlobalConstants.ENVIRONMENTAL_CONTROL_LITONG_FAN_SMOKE_SENSOR_DETECTOR_FUNCTION_CODE://02
                    pointModel.setPointName(GlobalConstants.ENVIRONMENTAL_CONTROL_LITONG_FAN_SMOKE_SENSOR_DETECTOR);//风机、烟感、探测器

                    String fssdLengthHex = responseInstructionList.get(2);//长度
                    pointModel.setTotalDataBytesHex(fssdLengthHex);

                    ArrayList<PointModel> fssdPointList = new ArrayList<>();
                    LinkedHashMap<String, String> fssdDataMapHex = new LinkedHashMap<>();

                    //解析风机、烟感、探测器
                    String fssdHex1 = responseInstructionList.get(3);//0000 0001 (寄存器地址从0000－0007）
                    fssdDataMapHex.put("1", fssdHex1);
                    int[] ints1 = ByteUtil.hexToBinToInts(fssdHex1);
                    for (int i = 0; i < ints1.length; i++) {
                        PointModel fssdModelPoint = new PointModel();

//                        QueryWrapper<DictEntity> fssdEntityQueryWrapper = new QueryWrapper<>();
//                        fssdEntityQueryWrapper.eq("is_deleted", 0);
//                        fssdEntityQueryWrapper.eq("type", "fan_smoke_sensor_detector");
//                        fssdEntityQueryWrapper.eq("name", deviceAddressHex + "_" + functionCodeHex + "_" + i);
//                        DictEntity fssdDictEntity = dictService.getOne(fssdEntityQueryWrapper);

                        String fanSmokeSensorDetector = dictUtil.getDictValue("fan_smoke_sensor_detector", i + "");
                        String[] split = fanSmokeSensorDetector.split("_");
                        String fanSmokeSensorDetectorSensorId = split[0];//传感器id
                        String parameterId = split[1];//参数id
                        String tag = split[2];//tag

                        ParamEntity paramEntity = paramService.getById(parameterId);


                        fssdModelPoint.setPointName(paramEntity.getObjType());

                        QueryWrapper<EthEntity> ethEntityQueryWrapper = new QueryWrapper<>();
                        ethEntityQueryWrapper.eq("is_deleted", 0);
                        ethEntityQueryWrapper.eq("sensor_model", paramEntity.getSensorNameCode());
                        ethEntityQueryWrapper.eq("tag", tag);
                        EthEntity ethServiceOne = ethService.getOne(ethEntityQueryWrapper);
//                        fssdModelPoint.setInstallationLocation(ethServiceOne.getInstallPostion());//安装位置
                        fssdModelPoint.setTag(ethServiceOne.getTag() + "_" + paramEntity.getTag());
                        fssdModelPoint.setSensorId(fanSmokeSensorDetectorSensorId);//传感器id


                        QueryWrapper<DeviceParamEntity> fssdQueryWrapper = new QueryWrapper<>();
                        fssdQueryWrapper.eq("is_deleted", 0);
                        fssdQueryWrapper.eq("device_id", ethServiceOne.getId());
                        fssdQueryWrapper.eq("param_id", parameterId);
                        DeviceParamEntity fssdDeviceParamEntity = deviceParamService.getOne(fssdQueryWrapper);

                        fssdModelPoint.setParamId(fssdDeviceParamEntity.getId());  //参数id
                        fssdModelPoint.setEthId(ethServiceOne.getId());   //eth的id
                        switch (i) {
                            case 0:
                            case 1:
                                //1:运行 0:停止


//                                if (0 == ints1[i]) {
//                                    fssdModelPoint.setValue(dictUtil.getDictValue("fan_smokedetector_presencedetction", "stop"));
//                                } else if (1 == ints1[i]) {
//                                    fssdModelPoint.setValue(dictUtil.getDictValue("fan_smokedetector_presencedetction", "start"));
//                                } else {
//                                    fssdModelPoint.setValue(ints1[i] + "");
//                                }

//                                break;
                            case 2:
                            case 3:
                                //1:手动 0:自动


//                                if (0 == ints1[i]) {
//                                    fssdModelPoint.setValue(dictUtil.getDictValue("fan_smokedetector_presencedetction", "auto"));
//                                } else if (1 == ints1[i]) {
//                                    fssdModelPoint.setValue(dictUtil.getDictValue("fan_smokedetector_presencedetction", "manual"));
//                                } else {
//                                    fssdModelPoint.setValue(ints1[i] + "");
//                                }
//                                break;
                            case 4:
                            case 6:
                                //1:无人 0:有人
                                //                                if (0 == ints1[i]) {
                                //                                    fssdModelPoint.setValue(dictUtil.getDictValue("fan_smokedetector_presencedetction", "someone"));
                                //                                } else if (1 == ints1[i]) {
                                //                                    fssdModelPoint.setValue(dictUtil.getDictValue("fan_smokedetector_presencedetction", "unmanned"));
                                //                                } else {
                                //                                    fssdModelPoint.setValue(ints1[i] + "");
                                //                                }
                                //                                break;
                            case 5:
                            case 7:
                                //1:正常 0:异常
//                                if (0 == ints1[i]) {
//                                    fssdModelPoint.setValue(dictUtil.getDictValue("fan_smokedetector_presencedetction", "abnormal"));
//                                } else if (1 == ints1[i]) {
//                                    fssdModelPoint.setValue(dictUtil.getDictValue("fan_smokedetector_presencedetction", "normal"));
//                                } else {
//                                    fssdModelPoint.setValue(ints1[i] + "");
//                                }
                                String value4 = getString(ints1[i] + "", paramEntity);
                                if (!StringUtils.isEmpty(value4)) {
                                    fssdModelPoint.setValue(value4);
                                } else {
                                    fssdModelPoint.setValue(ints1[i] + "");
                                }
                                break;

                        }


                        fssdModelPoint.setDataAddress("000" + i);
                        fssdPointList.add(fssdModelPoint);
                    }


                    //这个暂时不解析，根据电表可知没用到 TODO
//                    String fssdHex2 = responseInstructionList.get(4);
//                    fssdDataMapHex.put("2", fssdHex2);
//                    int[] ints2 = ByteUtil.hexToBinToInts(fssdHex2);


                    pointModel.setDataMapHex(fssdDataMapHex);
                    pointModel.setPointList(fssdPointList);//添加子point


                    pointModel.setTime(DateUtil.now());//设置时间
                    break;
                case GlobalConstants.ENVIRONMENTAL_CONTROL_LITONG_LIGHT_CONTROL_FUNCTION_CODE://03
                    pointModel.setPointName(GlobalConstants.ENVIRONMENTAL_CONTROL_LITONG_LIGHTCONTROL);//灯控

                    String lightControlLengthHex = responseInstructionList.get(2);//长度
                    pointModel.setTotalDataBytesHex(lightControlLengthHex);

                    ArrayList<PointModel> lightControlPointList = new ArrayList<>();
                    LinkedHashMap<String, String> lightControlDataMapHex = new LinkedHashMap<>();

                    //灯控
//                    String lightControl1 = responseInstructionList.get(3);//字节1没使用到
//                    lightControlDataMapHex.put("1", lightControl1);

                    String lightControl2 = responseInstructionList.get(4);
                    lightControlDataMapHex.put("2", lightControl2);
                    int[] ints2 = ByteUtil.hexToBinToInts(lightControl2);

                    //只用到bit0和bit1,所以只循环2次
                    for (int i = 0; i < 2; i++) {
                        PointModel fssdModelPoint = new PointModel();

//                        QueryWrapper<DictEntity> fssdEntityQueryWrapper = new QueryWrapper<>();
//                        fssdEntityQueryWrapper.eq("is_deleted", 0);
//                        fssdEntityQueryWrapper.eq("type", "light_control");
//                        fssdEntityQueryWrapper.eq("name", deviceAddressHex + "_" + functionCodeHex + "_" + i);
//                        DictEntity fssdDictEntity = dictService.getOne(fssdEntityQueryWrapper);

                        String lightControlValue = dictUtil.getDictValue("light_control", i + "");
                        String[] split = lightControlValue.split("_");
                        String lightControlSensorId = split[0];//传感器id
                        String lightControlParamrId = split[1];//参数id
                        SensorEntity lightControlSensorEntity = sensorService.getById(lightControlSensorId);
                        QueryWrapper<EthEntity> lightControlEthEntityQueryWrapper = new QueryWrapper<>();
                        lightControlEthEntityQueryWrapper.eq("is_deleted", 0);
                        lightControlEthEntityQueryWrapper.eq("sensor_model", lightControlSensorEntity.getSensorNameCode());
                        switch (deviceAddressHex) {
                            case "06":
                                lightControlEthEntityQueryWrapper.eq("tag", 1);//表示温湿度传感器eth行的tag
                                break;
                            case "07":
                                lightControlEthEntityQueryWrapper.eq("tag", 2);
                                break;
                        }
                        EthEntity lightControlEthEntity = ethService.getOne(lightControlEthEntityQueryWrapper);

                        ParamEntity lightControlParamEntity = paramService.getById(lightControlParamrId);

                        fssdModelPoint.setPointName(lightControlParamEntity.getObjType());


                        String value5 = getString(ints2[i] + "", lightControlParamEntity);
                        if (!StringUtils.isEmpty(value5)) {
                            fssdModelPoint.setValue(value5);
                        } else {
                            fssdModelPoint.setValue(ints2[i] + "");
                        }

//                        switch (ints2[i]) {
//                            case 0://关
//                                fssdModelPoint.setValue("关");
//                                break;
//                            case 1://开
//                                fssdModelPoint.setValue("开");
//                                break;
//                            default:
//                                fssdModelPoint.setValue(ints2[i] + "");
//                                break;
//                        }

                        fssdModelPoint.setSensorId(lightControlSensorId);


                        QueryWrapper<DeviceParamEntity> fssdQueryWrapper = new QueryWrapper<>();
                        fssdQueryWrapper.eq("is_deleted", 0);
                        fssdQueryWrapper.eq("device_id", lightControlEthEntity.getId());
                        fssdQueryWrapper.eq("param_id", lightControlParamrId);
                        DeviceParamEntity fssdDeviceParamEntity = deviceParamService.getOne(fssdQueryWrapper);


                        fssdModelPoint.setParamId(fssdDeviceParamEntity.getId());
                        fssdModelPoint.setEthId(lightControlEthEntity.getId());
                        fssdModelPoint.setDataAddress("000" + i);
                        fssdModelPoint.setTag(lightControlEthEntity.getTag() + "_" + lightControlParamEntity.getTag());
                        lightControlPointList.add(fssdModelPoint);
                    }


                    pointModel.setDataMapHex(lightControlDataMapHex);
                    pointModel.setPointList(lightControlPointList);//添加子point


                    pointModel.setTime(DateUtil.now());//设置时间
                    break;
                case GlobalConstants.ENVIRONMENTAL_CONTROL_LITONG_AIR_CONDITIONER_FUNCTION_CODE://06 表示反控或者获取反控回来的状态
                    if (GlobalConstants.ENVIRONMENTAL_CONTROL_LITONG_LIGHTING_CONTROL_SLAVE_ADDRESS.equals(deviceAddressHex)) {//设备地址06表示控制照明
                        sensorModel.setIsControl("1");//设置为反控，以免被查询出来
                        pointModel.setPointName(GlobalConstants.ENVIRONMENTAL_CONTROL_LITONG_LIGHTING_CONTROL);//照明控制

                        ArrayList<PointModel> lightingPointList = new ArrayList<>();
                        LinkedHashMap<String, String> lightingDataMapHex = new LinkedHashMap<>();

                        //解析


                        String lightingAddressHex = responseInstructionList.get(4) + responseInstructionList.get(5);//值
                        String registerAddressHexReplace = lightingAddressHex.replace(" ", "");
                        lightingDataMapHex.put("1", registerAddressHexReplace);

                        String value1Hex = responseInstructionList.get(3);
                        String value2Hex = responseInstructionList.get(5);
                        int[] ints = ByteUtil.hexToBinToInts(value2Hex);
                        for (int i = 0; i < 2; i++) {
                            PointModel lightingModelPoint = new PointModel();
                            QueryWrapper<DictEntity> lightingQueryWrapper = new QueryWrapper<>();
                            lightingQueryWrapper.eq("is_deleted", 0);
                            lightingQueryWrapper.eq("type", "lighting_reverse_control");
                            lightingQueryWrapper.eq("name", deviceAddressHex + "_" + functionCodeHex + "_" + value1Hex + "_" + i);
                            DictEntity lightingDictEntity = dictService.getOne(lightingQueryWrapper);
                            lightingModelPoint.setPointName(lightingDictEntity.getValue());
                            switch (ints[i]) {
                                case 0:
                                    lightingModelPoint.setValue("关");
                                    break;
                                case 1:
                                    lightingModelPoint.setValue("开");
                                    break;
                                default:
                                    lightingModelPoint.setValue(ints[i] + "");
                                    break;

                            }
                            lightingModelPoint.setDataAddress(responseInstructionList.get(2) + responseInstructionList.get(3));
                            lightingPointList.add(lightingModelPoint);
                        }


                        pointModel.setDataMapHex(lightingDataMapHex);
                        pointModel.setPointList(lightingPointList);//添加子point
                        pointModel.setTime(DateUtil.now());//设置时间
                    } else if (GlobalConstants.ENVIRONMENTAL_CONTROL_LITONG_AIRCONDITIONER_SLAVE_ADDRESS_1.equals(deviceAddressHex) || GlobalConstants.ENVIRONMENTAL_CONTROL_LITONG_AIRCONDITIONER_SLAVE_ADDRESS_2.equals(deviceAddressHex)) {//表示空调的状态
                        pointModel.setPointName(GlobalConstants.ENVIRONMENTAL_CONTROL_LITONG_AIR_CONDITIONER);//空调

                        ArrayList<PointModel> airConditionerPointList = new ArrayList<>();
                        LinkedHashMap<String, String> airConditionerDataMapHex = new LinkedHashMap<>();

                        //解析
                        PointModel airConditionerModelPoint = new PointModel();

//                        QueryWrapper<DictEntity> fssdEntityQueryWrapper = new QueryWrapper<>();
//                        fssdEntityQueryWrapper.eq("is_deleted", 0);
//                        fssdEntityQueryWrapper.eq("type", "air_conditioner");
//                        fssdEntityQueryWrapper.eq("name", deviceAddressHex + "_" + functionCodeHex);
//                        DictEntity fssdDictEntity = dictService.getOne(fssdEntityQueryWrapper);

                        String conditionerValue = dictUtil.getDictValue("conditioner", "conditioner_status");
                        String[] split = conditionerValue.split("_");
                        String conditionerSensorId = split[0];//传感器id
                        String conditionerParamId = split[1];//参数id

                        SensorEntity conditionerSensorEntity = sensorService.getById(conditionerSensorId);

                        QueryWrapper<EthEntity> conditionerEthEntityQueryWrapper = new QueryWrapper<>();
                        conditionerEthEntityQueryWrapper.eq("is_deleted", 0);
                        conditionerEthEntityQueryWrapper.eq("sensor_model", conditionerSensorEntity.getSensorNameCode());
                        switch (deviceAddressHex) {
                            case "08":
                                conditionerEthEntityQueryWrapper.eq("tag", 1);//表示温湿度传感器eth行的tag
                                break;
                            case "09":
                                conditionerEthEntityQueryWrapper.eq("tag", 2);
                                break;
                        }
                        EthEntity conditionerEthEntity = ethService.getOne(conditionerEthEntityQueryWrapper);
                        ParamEntity conditionerParamEntity = paramService.getById(conditionerParamId);


                        airConditionerModelPoint.setPointName(conditionerParamEntity.getObjType());

                        String registerAddressHex = responseInstructionList.get(2) + responseInstructionList.get(3);//寄存器地址
                        String registerAddressHexReplace = registerAddressHex.replace(" ", "");
                        airConditionerDataMapHex.put("1", registerAddressHexReplace);


                        String value6 = getStringAir(registerAddressHexReplace, conditionerParamEntity);
                        if (!StringUtils.isEmpty(value6)) {
                            airConditionerModelPoint.setValue(value6);
                        } else {
                            airConditionerModelPoint.setValue(registerAddressHexReplace);
                        }


//                        switch (registerAddressHexReplace) {
//                            case "00B9":
//                                airConditionerModelPoint.setValue("制冷");
//                                break;
//                            case "00BA":
//                                airConditionerModelPoint.setValue("制热");
//                                break;
//                            case "00BB":
//                                airConditionerModelPoint.setValue("停止");
//                                break;
//                            default:
//                                airConditionerModelPoint.setValue(registerAddressHexReplace);
//                                break;
//
//                        }
                        airConditionerModelPoint.setDataAddress(registerAddressHexReplace);
                        airConditionerModelPoint.setSensorId(conditionerSensorId);


                        QueryWrapper<DeviceParamEntity> airConditionerQueryWrapper = new QueryWrapper<>();
                        airConditionerQueryWrapper.eq("is_deleted", 0);
                        airConditionerQueryWrapper.eq("device_id", conditionerEthEntity.getId());
                        airConditionerQueryWrapper.eq("param_id", conditionerParamId);
                        DeviceParamEntity airConditionerDeviceParamEntity = deviceParamService.getOne(airConditionerQueryWrapper);


                        airConditionerModelPoint.setParamId(airConditionerDeviceParamEntity.getId());
                        airConditionerModelPoint.setEthId(conditionerEthEntity.getId());
                        airConditionerModelPoint.setTag(conditionerEthEntity.getTag() + "_" + conditionerParamEntity.getTag());

                        airConditionerPointList.add(airConditionerModelPoint);
                        pointModel.setDataMapHex(airConditionerDataMapHex);
                        pointModel.setPointList(airConditionerPointList);//添加子point
                        pointModel.setTime(DateUtil.now());//设置时间

                    }


                    break;


            }
            //CRC校验值
            pointModel.setCRCHex(responseInstructionList.get(responseInstructionList.size() - 2) + responseInstructionList.get(responseInstructionList.size() - 1));
            pointModel.setTime(DateUtil.now());//设置时间
        }
    }

    private String getStringAir(String i1, ParamEntity paramEntity) {
        HashMap<String, String> map = new HashMap<>();
        String value2 = paramEntity.getValue();//1:运行,0:停止
        String[] split3 = value2.split(",");//1:运行  0:停止
        int length = split3.length;
        for (int i = 0; i < length; i++) {
            String s1 = split3[i];//1:运行
            String[] split4 = s1.split(":");

            String s3 = split4[0];//1
            String s4 = split4[1];//运行
            map.put(s3, s4);
        }

        String airContrast = dictUtil.getDictValue("air_contrast", i1);
        return map.get(airContrast);
    }

    private String getString(String i1, ParamEntity paramEntity) {
        HashMap<String, String> map = new HashMap<>();
        String value2 = paramEntity.getValue();//1:运行,0:停止
        String[] split3 = value2.split(",");//1:运行  0:停止
        int length = split3.length;
        for (int i = 0; i < length; i++) {
            String s1 = split3[i];//1:运行
            String[] split4 = s1.split(":");

            String s3 = split4[0];//1
            String s4 = split4[1];//运行
            map.put(s3, s4);
        }
//        String s2 = split3[1];//0:停止
//        String[] split5 = s2.split(":");
//        String s5 = split5[0];//0
//        String s6 = split5[1];//停止
//        map.put(s5,s6);
        return map.get(i1);
    }

    /**
     * 电子围栏
     *
     * @param sensorModel
     */
    private void electronicFenceAnalysis(SensorModel sensorModel) {
        sensorModel.setType(GlobalConstants.TCP_CLIENT);
        List<PointModel> pointModelList = sensorModel.getPointModelList();
        for (PointModel pointModel : pointModelList) {
            switch (pointModel.getPointName()) {
                case GlobalConstants.ELECTRONIC_FENCE_STATUS_QUERY://状态查询

                    String responseInstruction1 = pointModel.getResponseInstruction();//00 00 0F 00 0B B8 FE 78 14 04 00 05 00 00 00 06 00 00
                    String replace = responseInstruction1.replace(" ", "");
                    if (StringUtils.isEmpty(replace)) {
                        return;
                    }
                    ArrayList<String> responseInstructionList = ByteUtil.strsToList(replace);
                    pointModel.setSlaveAddressHex(responseInstructionList.get(0));//起始位固定的
                    pointModel.setFunctionCodeHex(responseInstructionList.get(1));//控制命令
                    String dataLengthHex = responseInstructionList.get(2);//数据长度
                    pointModel.setTotalDataBytesHex(dataLengthHex);//数据长度
                    pointModel.setCRCHex(responseInstructionList.get(responseInstructionList.size() - 1));


                    ArrayList<PointModel> pointList = new ArrayList<>();

                    //解析16进制data
                    LinkedHashMap<String, String> dataMapHex = new LinkedHashMap<>();

                    //设备MAC地址
                    String macHex = responseInstructionList.get(3) + "-" + responseInstructionList.get(4) + "-" + responseInstructionList.get(5) + "-" +
                            responseInstructionList.get(6) + "-" + responseInstructionList.get(7) + "-" + responseInstructionList.get(8);
                    dataMapHex.put("1", macHex);

                    PointModel macModelPoint = new PointModel();
                    macModelPoint.setPointName("设备mac地址");
                    macModelPoint.setValue(macHex);
                    pointList.add(macModelPoint);

                    //设备类型
                    String deviceTypeHex = responseInstructionList.get(9);
                    dataMapHex.put("2", deviceTypeHex);


                    PointModel deviceTypePoint = new PointModel();
                    deviceTypePoint.setPointName("设备类型");

                    QueryWrapper<DictEntity> dictEntityQueryWrapper = new QueryWrapper<>();
//                    dictEntityQueryWrapper.eq("is_deleted",0);
                    dictEntityQueryWrapper.eq("type", "device_type");
                    dictEntityQueryWrapper.eq("value", deviceTypeHex);
                    List<DictEntity> dictEntityList = dictService.list(dictEntityQueryWrapper);
                    for (DictEntity dictEntity : dictEntityList) {
                        deviceTypePoint.setValue(dictEntity.getName());
                    }

                    pointList.add(deviceTypePoint);

                    //防区数量
                    String sectorNumHex = responseInstructionList.get(10);
                    dataMapHex.put("3", sectorNumHex);

                    Integer sectorNum = ByteUtil.hexStringToNum(sectorNumHex);

                    PointModel sectorNumPoint = new PointModel();
                    sectorNumPoint.setPointName("防区数量");
                    sectorNumPoint.setValue(sectorNum + "");
                    pointList.add(sectorNumPoint);

                    //防区状态
                    int index = 11;
                    for (int i = 1; i <= sectorNum; i++) {
                        String valueHex = responseInstructionList.get(index);
                        dataMapHex.put((3 + 1) + "", valueHex);

                        PointModel valuePoint = new PointModel();
                        valuePoint.setPointName("防区" + i + "状态");

                        QueryWrapper<DictEntity> valueQueryWrapper = new QueryWrapper<>();
                        valueQueryWrapper.eq("is_deleted", 0);
                        valueQueryWrapper.eq("type", "defense_zone_status");
                        valueQueryWrapper.eq("value", valueHex);
                        List<DictEntity> valueList = dictService.list(valueQueryWrapper);
                        for (DictEntity dictEntity : valueList) {
                            valuePoint.setValue(dictEntity.getName());
                        }

                        pointList.add(valuePoint);
                        index++;
                    }

                    pointModel.setDataMapHex(dataMapHex);
                    pointModel.setPointList(pointList);//添加子point


                    pointModel.setTime(DateUtil.now());//设置时间
                    break;
                case GlobalConstants.ELECTRONIC_FENCE_CLOTH_REMOVAL_CONTROL://布撤防设置

                    String clothRemovalResponseInstruction = pointModel.getResponseInstruction();//FF 02 02 02 01 FC
                    String clothRemovalResponseReplace = clothRemovalResponseInstruction.replace(" ", "");
                    if (StringUtils.isEmpty(clothRemovalResponseReplace)) {
                        return;
                    }
                    ArrayList<String> clothRemovalResponseInstructionList = ByteUtil.strsToList(clothRemovalResponseReplace);
                    pointModel.setSlaveAddressHex(clothRemovalResponseInstructionList.get(0));//起始位固定的
                    pointModel.setFunctionCodeHex(clothRemovalResponseInstructionList.get(1));//控制命令
                    String clothRemovalDataLengthHex = clothRemovalResponseInstructionList.get(2);//数据长度
                    pointModel.setTotalDataBytesHex(clothRemovalDataLengthHex);//数据长度
                    pointModel.setCRCHex(clothRemovalResponseInstructionList.get(clothRemovalResponseInstructionList.size() - 1));


                    ArrayList<PointModel> clothRemovalPointList = new ArrayList<>();

                    //解析16进制data
                    LinkedHashMap<String, String> clothRemovalDataMapHex = new LinkedHashMap<>();

                    //防区编号
                    String defenseCodeHex = clothRemovalResponseInstructionList.get(3);
                    clothRemovalDataMapHex.put("1", defenseCodeHex);

                    PointModel defenseCodeModelPoint = new PointModel();
                    defenseCodeModelPoint.setPointName("防区编码");
                    defenseCodeModelPoint.setValue(defenseCodeHex);
                    clothRemovalPointList.add(defenseCodeModelPoint);

                    //布撤防标志 布撤防标志定义： 01=布防，00=撤防
                    String clothRemovalMarkHex = clothRemovalResponseInstructionList.get(4);
                    clothRemovalDataMapHex.put("2", clothRemovalMarkHex);


                    PointModel clothRemovalMarkPointModel = new PointModel();
                    clothRemovalMarkPointModel.setPointName("布撤防标志");
                    if ("01".equals(clothRemovalMarkHex)) {
                        clothRemovalMarkPointModel.setValue("布防");
                    } else if ("00".equals(clothRemovalMarkHex)) {
                        clothRemovalMarkPointModel.setValue("撤防");
                    }
                    clothRemovalPointList.add(clothRemovalMarkPointModel);


                    pointModel.setDataMapHex(clothRemovalDataMapHex);
                    pointModel.setPointList(clothRemovalPointList);//添加子point


                    pointModel.setTime(DateUtil.now());//设置时间
                    break;
            }

        }
    }


    /**
     * XZL-801D微机综合保护测控装置协议解析
     *
     * @param sensorModel
     */
    private void protocolAnalysis(SensorModel sensorModel) {
        sensorModel.setType(GlobalConstants.TCP_CLIENT);
        List<PointModel> pointModelList = sensorModel.getPointModelList();
        for (PointModel pointModel : pointModelList) {
            switch (pointModel.getPointName()) {
                case GlobalConstants.HOST_INFORMATION://主机信息

                    String responseInstruction1 = pointModel.getResponseInstruction();//00 00 0F 00 0B B8 FE 78 14 04 00 05 00 00 00 06 00 00
                    String replace = responseInstruction1.replace(" ", "");
                    if (StringUtils.isEmpty(replace)) {
                        return;
                    }
                    ArrayList<String> responseInstructionList = ByteUtil.strsToList(replace);
                    String dataLengthHex = responseInstructionList.get(1) + responseInstructionList.get(2);//数据长度
                    pointModel.setTotalDataBytesHex(dataLengthHex);//数据长度
                    pointModel.setFunctionCodeHex(responseInstructionList.get(3));//功能码

                    ArrayList<PointModel> pointList = new ArrayList<>();

                    //解析16进制data
                    LinkedHashMap<String, String> dataMapHex = new LinkedHashMap<>();

                    //主机型号
                    String hostModelHex = responseInstructionList.get(4) + responseInstructionList.get(5);
                    dataMapHex.put("1", hostModelHex);

                    PointModel hostModelPoint = new PointModel();
                    hostModelPoint.setPointName("主机型号");
                    hostModelPoint.setValue(ByteUtil.hexStringToNum(hostModelHex) + "");
                    pointList.add(hostModelPoint);

                    //最大总线回路数
                    String maximumBusLoopsHex = responseInstructionList.get(6);
                    dataMapHex.put("2", maximumBusLoopsHex);
                    PointModel maximumBusLoopsPoint = new PointModel();
                    maximumBusLoopsPoint.setPointName("最大总线回路数");
                    maximumBusLoopsPoint.setValue(ByteUtil.hexStringToNum(maximumBusLoopsHex) + "");
                    pointList.add(maximumBusLoopsPoint);

                    //单总线回路模块最大容量
                    String moduleMaximumCapacityHex = responseInstructionList.get(7);
                    dataMapHex.put("3", moduleMaximumCapacityHex);
                    PointModel moduleMaximumCapacityPoint = new PointModel();
                    moduleMaximumCapacityPoint.setPointName("单总线回路模块最大容量");
                    moduleMaximumCapacityPoint.setValue(ByteUtil.hexStringToNum(moduleMaximumCapacityHex) + "");
                    pointList.add(moduleMaximumCapacityPoint);

                    //单模块配节探头最大容量
                    String probeMaximumCapacityHex = responseInstructionList.get(8);
                    dataMapHex.put("4", probeMaximumCapacityHex);
                    PointModel probeMaximumCapacityPoint = new PointModel();
                    probeMaximumCapacityPoint.setPointName("单模块配节探头最大容量");
                    probeMaximumCapacityPoint.setValue(ByteUtil.hexStringToNum(probeMaximumCapacityHex) + "");
                    pointList.add(probeMaximumCapacityPoint);

                    //在线总线回路数
                    String busLoopNumberHex = responseInstructionList.get(9);
                    dataMapHex.put("5", busLoopNumberHex);
                    PointModel busLoopNumberPoint = new PointModel();
                    busLoopNumberPoint.setPointName("在线总线回路数");
                    busLoopNumberPoint.setValue(ByteUtil.hexStringToNum(busLoopNumberHex) + "");
                    pointList.add(busLoopNumberPoint);

                    //系统模块总数
                    String totalNumberModulesHex = responseInstructionList.get(10) + responseInstructionList.get(11);
                    dataMapHex.put("6", totalNumberModulesHex);
                    PointModel totalNumberModulesPoint = new PointModel();
                    totalNumberModulesPoint.setPointName("系统模块总数");
                    totalNumberModulesPoint.setValue(ByteUtil.hexStringToNum(totalNumberModulesHex) + "");
                    pointList.add(totalNumberModulesPoint);

                    //模块在线总数
                    String totalNumberModulesOnlineHex = responseInstructionList.get(12) + responseInstructionList.get(13);
                    dataMapHex.put("7", totalNumberModulesOnlineHex);
                    PointModel totalNumberModulesOnlinePoint = new PointModel();
                    totalNumberModulesOnlinePoint.setPointName("模块在线总数");
                    totalNumberModulesOnlinePoint.setValue(ByteUtil.hexStringToNum(totalNumberModulesOnlineHex) + "");
                    pointList.add(totalNumberModulesOnlinePoint);

                    //故障总数
                    String totalNumberFailuresHex = responseInstructionList.get(14) + responseInstructionList.get(15);
                    dataMapHex.put("8", totalNumberFailuresHex);
                    PointModel totalNumberFailuresHexPoint = new PointModel();
                    totalNumberFailuresHexPoint.setPointName("故障总数");
                    totalNumberFailuresHexPoint.setValue(ByteUtil.hexStringToNum(totalNumberFailuresHex) + "");
                    pointList.add(totalNumberFailuresHexPoint);

                    //报警总数
                    String totalNumberAlarmsHex = responseInstructionList.get(16) + responseInstructionList.get(17);
                    dataMapHex.put("9", totalNumberAlarmsHex);
                    PointModel totalNumberAlarmsHexPoint = new PointModel();
                    totalNumberAlarmsHexPoint.setPointName("报警总数");
                    totalNumberAlarmsHexPoint.setValue(ByteUtil.hexStringToNum(totalNumberAlarmsHex) + "");
                    pointList.add(totalNumberAlarmsHexPoint);

                    pointModel.setDataMapHex(dataMapHex);
                    pointModel.setPointList(pointList);//添加子point


                    pointModel.setTime(DateUtil.now());//设置时间
                    break;
                case GlobalConstants.COMPLEX_PROTECTION_ALERT://XZL-801D微机综合保护测控装置-告警
                    String alertResponseInstruction = pointModel.getResponseInstruction();

                    String replace1 = alertResponseInstruction.replace(" ", "");
                    if (StringUtils.isEmpty(replace1)) {
                        return;
                    }
                    ArrayList<String> alertResponseInstructionList = ByteUtil.strsToList(replace1);

                    pointModel.setSlaveAddressHex(alertResponseInstructionList.get(0));//从机地址
                    pointModel.setFunctionCodeHex(alertResponseInstructionList.get(1));//功能码
                    String alertTotalDataBytesHex = alertResponseInstructionList.get(2);
                    pointModel.setTotalDataBytesHex(alertTotalDataBytesHex);//数据字节总数


                    //解析16进制data
                    Integer alertTotalDataBytes = ByteUtil.hexStringToNum(alertTotalDataBytesHex);
                    LinkedHashMap<String, String> alertDataMapHex = new LinkedHashMap<>();

                    ArrayList<PointModel> alertPointList = new ArrayList<>();

                    int alertIndex = 3;
                    for (int i = 0; i < alertTotalDataBytes; i++) {
                        String alertValueHex = alertResponseInstructionList.get(alertIndex);
                        alertDataMapHex.put(i + "", alertValueHex);
                        alertIndex++;
                    }
                    pointModel.setDataMapHex(alertDataMapHex);


                    //解析
                    //时间
                    PointModel timePointModel = new PointModel();
                    timePointModel.setPointName(GlobalConstants.COMPLEX_PROTECTION_ALERT_TIME);
                    int millisecondHigh = Integer.parseInt(alertResponseInstructionList.get(3));//毫秒高字节
                    int millisecondLow = Integer.parseInt(alertResponseInstructionList.get(4));//毫秒低字节
                    String millisecond = millisecondLow + "" + millisecondHigh;//毫秒

                    String second = alertResponseInstructionList.get(5);//秒
                    String minute = alertResponseInstructionList.get(6);//分
                    String hour = alertResponseInstructionList.get(7);//时
                    String day = alertResponseInstructionList.get(8);//日
                    String month = alertResponseInstructionList.get(9);//月
                    String year = alertResponseInstructionList.get(10);//年


                    timePointModel.setValue(year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second + "." + millisecond);
                    alertPointList.add(timePointModel);
                    //报告属性
                    PointModel reportPointModel = new PointModel();
                    reportPointModel.setPointName(GlobalConstants.COMPLEX_PROTECTION_ALERT_REPORT_PROPERTIES);
                    Integer reportInt = ByteUtil.hexStringToNum(alertResponseInstructionList.get(11));
                    switch (reportInt) {
                        case 1://遥信
                            reportPointModel.setValue("遥信");
                            break;
                        case 2://保护
                            reportPointModel.setValue("保护");
                            break;
                        case 3://告警
                            reportPointModel.setValue("告警");
                            break;
                        default:
                            reportPointModel.setValue(reportInt + "");
                            break;
                    }
                    reportPointModel.setDataAddress(ByteUtil.numToLengthHexStr(8, 4));
                    alertPointList.add(reportPointModel);
                    //记录的索引号
                    PointModel indexPointModel = new PointModel();
                    indexPointModel.setPointName(GlobalConstants.COMPLEX_PROTECTION_ALERT_RECORD_INDEX_NUMBER);
                    indexPointModel.setValue(ByteUtil.hexStringToNum(alertResponseInstructionList.get(12)) + "");
                    indexPointModel.setDataAddress("09");
                    indexPointModel.setDataAddress(ByteUtil.numToLengthHexStr(9, 4));
                    alertPointList.add(indexPointModel);
                    //动作值高
                    PointModel highPointModel = new PointModel();
                    highPointModel.setPointName(GlobalConstants.COMPLEX_PROTECTION_ALERT_HIGH_ACTION_VALUE);
                    int i = ByteUtil.hexStringToNum(alertResponseInstructionList.get(13)) + ByteUtil.hexStringToNum(alertResponseInstructionList.get(14));
                    highPointModel.setValue(i + "");
                    alertPointList.add(highPointModel);
                    //动作值
                    PointModel valuePointModel = new PointModel();
                    valuePointModel.setPointName(GlobalConstants.COMPLEX_PROTECTION_ALERT_ACTION_VALUE_ATTRIBUTE);
                    valuePointModel.setValue(ByteUtil.hexStringToNum(alertResponseInstructionList.get(17)) + "");
                    valuePointModel.setDataAddress(ByteUtil.numToLengthHexStr(14, 4));
                    alertPointList.add(valuePointModel);

                    pointModel.setPointList(alertPointList);//添加子point


                    //CRC校验值
                    pointModel.setCRCHex(alertResponseInstructionList.get(alertResponseInstructionList.size() - 2) + alertResponseInstructionList.get(alertResponseInstructionList.size() - 1));

                    pointModel.setTime(DateUtil.now());//设置时间
                    break;
            }

        }
    }
}
