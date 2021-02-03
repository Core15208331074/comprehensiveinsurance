package com.scdy.comprehensiveinsurance.service.analysis.impl;
/**
 * 串口协议解析.
 */

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scdy.comprehensiveinsurance.constant.GlobalConstants;
import com.scdy.comprehensiveinsurance.entity.DeviceParamEntity;
import com.scdy.comprehensiveinsurance.entity.DictEntity;
import com.scdy.comprehensiveinsurance.entity.ParamEntity;
import com.scdy.comprehensiveinsurance.model.PointModel;
import com.scdy.comprehensiveinsurance.model.SensorModel;
import com.scdy.comprehensiveinsurance.service.DeviceParamService;
import com.scdy.comprehensiveinsurance.service.DictService;
import com.scdy.comprehensiveinsurance.service.ParamService;
import com.scdy.comprehensiveinsurance.service.analysis.ProtocolAnalysisService;
import com.scdy.comprehensiveinsurance.utils.ByteUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Service
@Transactional
public class ComProtocolAnalysisServiceImpl implements ProtocolAnalysisService {
    @Autowired
    private DictService dictService;
    @Autowired
    private ParamService paramService;
    @Autowired
    private DeviceParamService deviceParamService;


    @Override
    public SensorModel analysis(SensorModel sensorModel) throws Exception {
        List<PointModel> pointModelList = sensorModel.getPointModelList();
        String sensorNameId = sensorModel.getSensorNameId();
        for (PointModel pointModel : pointModelList) {
            switch (sensorNameId) {
                case "1"://XZL-801D 微机综合保护测控装置
                    protocolAnalysis(sensorModel);
                    break;
                case "3"://铁芯夹件
                    coreClampAnalysis(sensorModel);
                    break;
                case "5"://数采控制器
                    dataCollectionAnalysis(sensorModel);
                    break;
                case "14"://峨山蓄电池
                    mountEBatteryAnalysis(sensorModel);
                    break;
            }


        }


        return sensorModel;
    }

    /**
     * 峨山蓄电池
     *
     * @param sensorModel
     */
    private void mountEBatteryAnalysis(SensorModel sensorModel) {
        sensorModel.setType(GlobalConstants.COM);
        List<PointModel> pointModelList = sensorModel.getPointModelList();
        for (PointModel pointModel : pointModelList) {
            switch (pointModel.getPointName()) {
                case GlobalConstants.BATTERY_MOUNT_E_SINGLE_VOLTAGE_1_100://单体电压

                    //设置数据起始地址
                    String requestInstruction = pointModel.getRequestInstruction();
                    String replace = requestInstruction.replace(" ", "");
                    if (StringUtils.isEmpty(replace)) {
                        return;
                    }
                    ArrayList<String> requestInstructionList = ByteUtil.strsToList(replace);
                    String dataStartAddressHex = requestInstructionList.get(2) + requestInstructionList.get(3);//数据起始地址16进制
                    pointModel.setDataStartAddress(dataStartAddressHex);


                    //获取返回指令的数组list
                    String responseInstruction = pointModel.getResponseInstruction();
                    String replace1 = responseInstruction.replace(" ", "");
                    if (StringUtils.isEmpty(replace1)) {
                        return;
                    }
                    ArrayList<String> responseInstructionList = ByteUtil.strsToList(replace1);

                    //设置从机地址
                    String slaveAddressHex = responseInstructionList.get(0);
                    pointModel.setSlaveAddressHex(slaveAddressHex);

                    //设置功能码
                    pointModel.setFunctionCodeHex(responseInstructionList.get(1));

                    //设置数据字节总数
                    String totalDataBytesHex = responseInstructionList.get(2);
                    pointModel.setTotalDataBytesHex(totalDataBytesHex);


                    //开始解析point数据
                    ArrayList<PointModel> pointList = new ArrayList<>();
                    LinkedHashMap<String, String> dataMapHex = new LinkedHashMap<>();


                    //循环解析
                    int totalDataBytesDec = ByteUtil.hexStringToNum(totalDataBytesHex);//10进制
                    double div = NumberUtil.div(totalDataBytesDec, 2);
                    int index = 3;
                    for (int i = 0; i < div; i++) {
                        Integer paramTag=i+1;//参数tag

                        StringBuilder stringBuilder = new StringBuilder();
                        for (int j = 0; j < 2; j++) {
                            stringBuilder.append(responseInstructionList.get(index));
                            index++;
                        }



                        //创建point
                        PointModel singleVoltagePointModel = new PointModel();

                        //设置名称
                        singleVoltagePointModel.setPointName("单体电压");

                        //设置原始值
                        String valueHex = stringBuilder.toString();
                        singleVoltagePointModel.setOriginalValue(valueHex);

                        //设置值
                        int valueDec = ByteUtil.hexStringToNum(valueHex);
                        singleVoltagePointModel.setValue(NumberUtil.mul(valueDec, 0.001) + "");

                        //设置数据地址
                        Integer dataStartAddressDec = ByteUtil.hexStringToNum(dataStartAddressHex);
                        singleVoltagePointModel.setDataAddress(ByteUtil.numToLengthHexStr(dataStartAddressDec + i,4));

                        //设置sensor_id
                        String sensorId = sensorModel.getSensorId();
                        if(!StringUtils.isEmpty(sensorId)){
                            singleVoltagePointModel.setSensorId(sensorId);
                        }

                        //设置device_id
                        String ethId = sensorModel.getEthId();
                        if(!StringUtils.isEmpty(ethId)){
                            singleVoltagePointModel.setEthId(ethId);
                        }

                        //设置参数id
                        QueryWrapper<ParamEntity> paramEntityQueryWrapper = new QueryWrapper<>();
                        paramEntityQueryWrapper.eq("is_deleted",0);
                        paramEntityQueryWrapper.eq("sensor_name_code",sensorModel.getSensorNameId());
                        paramEntityQueryWrapper.eq("parameter","单体电压");
                        paramEntityQueryWrapper.eq("tag",paramTag);
                        ParamEntity paramEntity = paramService.getOne(paramEntityQueryWrapper);
                        String paramId = paramEntity.getId();
                        if(!StringUtils.isEmpty(ethId)){
                            QueryWrapper<DeviceParamEntity> deviceParamEntityQueryWrapper = new QueryWrapper<>();
                            deviceParamEntityQueryWrapper.eq("is_deleted",0);
                            deviceParamEntityQueryWrapper.eq("device_id",ethId);
                            deviceParamEntityQueryWrapper.eq("param_id",paramId);
                            DeviceParamEntity deviceParamEntity = deviceParamService.getOne(deviceParamEntityQueryWrapper);
                            singleVoltagePointModel.setParamId(deviceParamEntity.getId());
                        }
                        pointList.add(singleVoltagePointModel);

                    }

                    pointModel.setDataMapHex(dataMapHex);
                    pointModel.setPointList(pointList);//添加子point

                    //CRC校验值
                    pointModel.setCRCHex(responseInstructionList.get(responseInstructionList.size() - 2) + responseInstructionList.get(responseInstructionList.size() - 1));
                    pointModel.setTime(DateUtil.now());//设置时间
                    break;

            }
        }
    }

    /**
     * 数采控制器协议解析
     *
     * @param sensorModel
     */
    private void dataCollectionAnalysis(SensorModel sensorModel) {
        sensorModel.setType(GlobalConstants.COM);
        List<PointModel> pointModelList = sensorModel.getPointModelList();
        for (PointModel pointModel : pointModelList) {
            switch (pointModel.getPointName()) {
                case GlobalConstants.DATA_ACQUISITION_CONTROLLER_RELAY://数采控制器-继电器
                    String requestInstruction = pointModel.getRequestInstruction();//01 01 00 00 00 0A BC 0D
                    String replace = requestInstruction.replace(" ", "");
                    if (StringUtils.isEmpty(replace)) {
                        return;
                    }
                    ArrayList<String> requestInstructionList = ByteUtil.strsToList(replace);
                    String dataStartAddressHex = requestInstructionList.get(2) + requestInstructionList.get(3);//数据起始地址16进制
                    pointModel.setDataStartAddress(dataStartAddressHex);//数据起始地址

                    //01 01 02 01 01 B9 FC
                    String responseInstruction = pointModel.getResponseInstruction();

                    ArrayList<String> responseInstructionList = ByteUtil.strsToList(responseInstruction.replace(" ", ""));

                    if (responseInstructionList.size() < 1) {
                        return;
                    }
                    String slaveAddressHex = responseInstructionList.get(0);
                    pointModel.setSlaveAddressHex(slaveAddressHex);//从机地址
                    pointModel.setFunctionCodeHex(responseInstructionList.get(1));//功能码
                    String totalDataBytesHex = responseInstructionList.get(2);
                    pointModel.setTotalDataBytesHex(totalDataBytesHex);//数据字节总数


                    ArrayList<PointModel> pointList = new ArrayList<>();
                    LinkedHashMap<String, String> dataMapHex = new LinkedHashMap<>();


                    //解析16进制data

                    //继电器
                    ArrayList<Integer> intList = new ArrayList<>();
                    String relayHighByteHex = responseInstructionList.get(3);//高字节
                    int[] highByteInts = ByteUtil.hexToBinToInts(relayHighByteHex);
                    CollectionUtil.addAll(intList, highByteInts);

                    String relayLowByteHex = responseInstructionList.get(4);//低字节
                    int[] lowByteInts = ByteUtil.hexToBinToInts(relayLowByteHex);
                    CollectionUtil.addAll(intList, lowByteInts);


                    for (int i = 0; i < intList.size(); i++) {
                        Integer integer = intList.get(i);
                        dataMapHex.put((i + 1) + "", integer + "");

                        PointModel relayPointModel = new PointModel();

                        QueryWrapper<DictEntity> dictEntityQueryWrapper = new QueryWrapper<>();
                        dictEntityQueryWrapper.eq("is_deleted", 0);
                        dictEntityQueryWrapper.eq("type", "data_acquisition_controller");
                        Integer slaveAddressDec = ByteUtil.hexStringToNum(slaveAddressHex);
                        dictEntityQueryWrapper.eq("name", slaveAddressDec + "_" + (i + 1));
                        DictEntity dictEntity = dictService.getOne(dictEntityQueryWrapper);

                        if (StringUtils.isEmpty(dictEntity)) {
                            relayPointModel.setPointName("第" + (i + 1) + "路");
                        } else {
                            relayPointModel.setPointName(dictEntity.getValue());
                        }


                        if (0 == integer) {
                            relayPointModel.setValue("断开");
                        } else if (1 == integer) {
                            relayPointModel.setValue("闭合");
                        }

                        relayPointModel.setDataAddress(dataStartAddressHex);//数据地址

                        //设置数据地址自增
                        Integer integer1 = ByteUtil.hexStringToNum(dataStartAddressHex);
                        int i1 = integer1 + 1;
                        dataStartAddressHex = ByteUtil.numToLengthHexStr(i1, 4);

                        pointList.add(relayPointModel);
                    }


                    pointModel.setDataMapHex(dataMapHex);
                    pointModel.setPointList(pointList);//添加子point

                    //CRC校验值
                    pointModel.setCRCHex(responseInstructionList.get(responseInstructionList.size() - 2) + responseInstructionList.get(responseInstructionList.size() - 1));
                    pointModel.setTime(DateUtil.now());//设置时间
                    break;
                case GlobalConstants.DATA_ACQUISITION_CONTROLLER_SWITCH://数采控制器-开关量
                    String switchRequestInstruction = pointModel.getRequestInstruction();//01 02 00 00 00 0C 78 0f
                    String switchReplace = switchRequestInstruction.replace(" ", "");
                    if (StringUtils.isEmpty(switchReplace)) {
                        return;
                    }
                    ArrayList<String> switchRequestInstructionList = ByteUtil.strsToList(switchReplace);
                    String switchStartAddressHex = switchRequestInstructionList.get(2) + switchRequestInstructionList.get(3);//数据起始地址16进制
                    pointModel.setDataStartAddress(switchStartAddressHex);//数据起始地址

                    //01 02 02 01 01 B9 B8
                    String switchResponseInstruction = pointModel.getResponseInstruction();

                    ArrayList<String> switchResponseInstructionList = ByteUtil.strsToList(switchResponseInstruction.replace(" ", ""));

                    if (switchResponseInstructionList.size() < 1) {
                        return;
                    }
                    String slaveAddressHex1 = switchResponseInstructionList.get(0);
                    pointModel.setSlaveAddressHex(slaveAddressHex1);//从机地址
                    pointModel.setFunctionCodeHex(switchResponseInstructionList.get(1));//功能码
                    String switchTotalDataBytesHex = switchResponseInstructionList.get(2);
                    pointModel.setTotalDataBytesHex(switchTotalDataBytesHex);//数据字节总数


                    ArrayList<PointModel> switchPointList = new ArrayList<>();
                    LinkedHashMap<String, String> switchDataMapHex = new LinkedHashMap<>();


                    //解析16进制data

                    //开关量
                    ArrayList<Integer> switchIntList = new ArrayList<>();
                    String switchHighByteHex = switchResponseInstructionList.get(3);//高字节
                    int[] switchHighByteInts = ByteUtil.hexToBinToInts(switchHighByteHex);
                    CollectionUtil.addAll(switchIntList, switchHighByteInts);

                    String switchLowByteHex = switchResponseInstructionList.get(4);//低字节
                    int[] switchLowByteInts = ByteUtil.hexToBinToInts(switchLowByteHex);
                    CollectionUtil.addAll(switchIntList, switchLowByteInts);


                    for (int i = 0; i < switchIntList.size(); i++) {
                        Integer integer = switchIntList.get(i);
                        switchDataMapHex.put((i + 1) + "", integer + "");

                        PointModel relayPointModel = new PointModel();


                        QueryWrapper<DictEntity> dictEntityQueryWrapper = new QueryWrapper<>();
                        dictEntityQueryWrapper.eq("is_deleted", 0);
                        dictEntityQueryWrapper.eq("type", "data_acquisition_controller");
                        Integer slaveAddressDec = ByteUtil.hexStringToNum(slaveAddressHex1);
                        dictEntityQueryWrapper.eq("name", slaveAddressDec + "_" + (i + 1));
                        DictEntity dictEntity = dictService.getOne(dictEntityQueryWrapper);

                        if (StringUtils.isEmpty(dictEntity)) {
                            relayPointModel.setPointName("第" + (i + 1) + "路");
                        } else {
                            relayPointModel.setPointName(dictEntity.getValue());
                        }


                        if (0 == integer) {
                            relayPointModel.setValue("断开");
                        } else if (1 == integer) {
                            relayPointModel.setValue("闭合");
                        }

                        relayPointModel.setDataAddress(switchStartAddressHex);//数据地址

                        //设置数据地址自增
                        Integer integer1 = ByteUtil.hexStringToNum(switchStartAddressHex);
                        int i1 = integer1 + 1;
                        switchStartAddressHex = ByteUtil.numToLengthHexStr(i1, 4);

                        switchPointList.add(relayPointModel);
                    }


                    pointModel.setDataMapHex(switchDataMapHex);
                    pointModel.setPointList(switchPointList);//添加子point

                    //CRC校验值
                    pointModel.setCRCHex(switchResponseInstructionList.get(switchResponseInstructionList.size() - 2) + switchResponseInstructionList.get(switchResponseInstructionList.size() - 1));
                    pointModel.setTime(DateUtil.now());//设置时间
                    break;
            }
        }
    }

    /**
     * 铁芯夹件协议解析
     *
     * @param sensorModel
     */
    private void coreClampAnalysis(SensorModel sensorModel) {
        sensorModel.setType(GlobalConstants.COM);
        List<PointModel> pointModelList = sensorModel.getPointModelList();
        for (PointModel pointModel : pointModelList) {
            switch (pointModel.getPointName()) {
                case GlobalConstants.CORE_CLAMP_READ_TIME_CURRENT_DATA://铁芯夹件-读取时间和电流数据
                    String requestInstruction = pointModel.getRequestInstruction();//01 03 00 00 00 0B 04 0D
                    String replace = requestInstruction.replace(" ", "");
                    if (StringUtils.isEmpty(replace)) {
                        return;
                    }
                    ArrayList<String> requestInstructionList = ByteUtil.strsToList(replace);
                    String dataStartAddressHex = requestInstructionList.get(2) + requestInstructionList.get(3);//数据起始地址16进制
                    pointModel.setDataStartAddress(dataStartAddressHex);//数据起始地址

                    //01 03 16
                    // 00 13 00 01 00 0F 00 0E 00 22 00 28 00 64 00 12 D6 80 00 00 00 00
                    // 44 A3
                    String responseInstruction = pointModel.getResponseInstruction();

                    ArrayList<String> responseInstructionList = ByteUtil.strsToList(responseInstruction.replace(" ", ""));

                    if (responseInstructionList.size() < 1) {
                        return;
                    }
                    pointModel.setSlaveAddressHex(responseInstructionList.get(0));//从机地址
                    pointModel.setFunctionCodeHex(responseInstructionList.get(1));//功能码
                    String totalDataBytesHex = responseInstructionList.get(2);
                    pointModel.setTotalDataBytesHex(totalDataBytesHex);//数据字节总数


                    ArrayList<PointModel> pointList = new ArrayList<>();
                    LinkedHashMap<String, String> dataMapHex = new LinkedHashMap<>();


                    //解析16进制data

                    //时间
                    PointModel timePointModel = new PointModel();
                    String yearHex = responseInstructionList.get(3) + responseInstructionList.get(4);//年
                    Integer year = ByteUtil.hexStringToNum(yearHex);
                    String monthHex = responseInstructionList.get(5) + responseInstructionList.get(6);//月
                    Integer month = ByteUtil.hexStringToNum(monthHex);
                    String dayHex = responseInstructionList.get(7) + responseInstructionList.get(8);//日
                    Integer day = ByteUtil.hexStringToNum(dayHex);
                    String hourHex = responseInstructionList.get(9) + responseInstructionList.get(10);//时
                    Integer hour = ByteUtil.hexStringToNum(hourHex);
                    String minutesHex = responseInstructionList.get(11) + responseInstructionList.get(12);//分
                    Integer minutes = ByteUtil.hexStringToNum(minutesHex);
                    String secondsHex = responseInstructionList.get(13) + responseInstructionList.get(14);//秒
                    Integer seconds = ByteUtil.hexStringToNum(secondsHex);
                    dataMapHex.put("1", yearHex + monthHex + dayHex + hourHex + minutesHex + secondsHex);

                    String value = year + "年" + month + "月" + day + "日" + hour + "时" + minutes + "分" + seconds + "秒";

                    timePointModel.setPointName(GlobalConstants.CORE_CLAMP_READ_TIME_CURRENT_DATA_TIME);
                    timePointModel.setValue(value);
                    timePointModel.setDataAddress("0000-0005");
                    pointList.add(timePointModel);

                    //铁芯电流
                    PointModel currentPointModel = new PointModel();

                    String currentHex = responseInstructionList.get(17) + responseInstructionList.get(18) + responseInstructionList.get(19) + responseInstructionList.get(20);
                    dataMapHex.put("2", currentHex);
                    double current = ByteUtil.hexStringToNum(currentHex) * 0.01;

                    currentPointModel.setPointName(GlobalConstants.CORE_CLAMP_READ_TIME_CURRENT_DATA_CURRENT);
                    currentPointModel.setValue(current + "");
                    currentPointModel.setUnit("mA");
                    currentPointModel.setDataAddress("0007-0008");
                    pointList.add(currentPointModel);


                    //夹件电流
                    PointModel clampPointModel = new PointModel();

                    String clampHex = responseInstructionList.get(21) + responseInstructionList.get(22) + responseInstructionList.get(23) + responseInstructionList.get(24);
                    dataMapHex.put("3", clampHex);
                    double clamp = ByteUtil.hexStringToNum(clampHex) * 0.01;

                    clampPointModel.setPointName(GlobalConstants.CORE_CLAMP_READ_TIME_CURRENT_DATA_CLAMP);
                    clampPointModel.setValue(clamp + "");
                    clampPointModel.setUnit("mA");
                    clampPointModel.setDataAddress("0009-000A");
                    pointList.add(clampPointModel);


                    pointModel.setDataMapHex(dataMapHex);
                    pointModel.setPointList(pointList);//添加子point


                    //CRC校验值
                    pointModel.setCRCHex(responseInstructionList.get(responseInstructionList.size() - 2) + responseInstructionList.get(responseInstructionList.size() - 1));

                    pointModel.setTime(DateUtil.now());//设置时间
                    break;
                case GlobalConstants.CORE_CLAMP_ALARM_SIGN://报警标志
                    String alarmRequestInstruction = pointModel.getRequestInstruction();//01 03 0F 00 00 02 C7 1F
                    String alarmReplace = alarmRequestInstruction.replace(" ", "");
                    if (StringUtils.isEmpty(alarmReplace)) {
                        return;
                    }
                    ArrayList<String> alarmRequestInstructionList = ByteUtil.strsToList(alarmReplace);
                    String alarmDataStartAddressHex = alarmRequestInstructionList.get(2) + alarmRequestInstructionList.get(3);//数据起始地址16进制
                    pointModel.setDataStartAddress(alarmDataStartAddressHex);//数据起始地址

                    //01 03 04 00 00 00 00 FA 33
                    String alarmResponseInstruction = pointModel.getResponseInstruction();

                    ArrayList<String> alarmResponseInstructionList = ByteUtil.strsToList(alarmResponseInstruction.replace(" ", ""));

                    if (alarmResponseInstructionList.size() < 1) {
                        return;
                    }
                    pointModel.setSlaveAddressHex(alarmResponseInstructionList.get(0));//从机地址
                    pointModel.setFunctionCodeHex(alarmResponseInstructionList.get(1));//功能码
                    String alarmTotalDataBytesHex = alarmResponseInstructionList.get(2);
                    pointModel.setTotalDataBytesHex(alarmTotalDataBytesHex);//数据字节总数


                    ArrayList<PointModel> alarmPointList = new ArrayList<>();
                    LinkedHashMap<String, String> alarmDataMapHex = new LinkedHashMap<>();


                    //解析16进制data

                    //铁芯电流报警标志
                    PointModel coreAlarmPointModel = new PointModel();
                    String coreAlarmHex = alarmResponseInstructionList.get(3) + alarmResponseInstructionList.get(4);
                    alarmDataMapHex.put("1", coreAlarmHex);
                    coreAlarmPointModel.setPointName(GlobalConstants.CORE_CLAMP_ALARM_SIGN_CORE_CURRENT_ALARM_MARK);
                    if ("0001".equals(coreAlarmHex.replace(" ", ""))) {
                        coreAlarmPointModel.setValue("有报警");
                    } else if ("0000".equals(coreAlarmHex.replace(" ", ""))) {
                        coreAlarmPointModel.setValue("无报警");

                    }
                    coreAlarmPointModel.setDataAddress(alarmDataStartAddressHex);
                    alarmPointList.add(coreAlarmPointModel);

                    //夹件电流报警标志
                    PointModel clampAlarmPointModel = new PointModel();
                    String clampAlarmHex = alarmResponseInstructionList.get(5) + alarmResponseInstructionList.get(6);
                    alarmDataMapHex.put("2", clampAlarmHex);
                    clampAlarmPointModel.setPointName(GlobalConstants.CORE_CLAMP_ALARM_SIGN_CLAMP_CURRENT_ALARM_SIGN);
                    if ("0001".equals(clampAlarmHex.replace(" ", ""))) {
                        clampAlarmPointModel.setValue("有报警");
                    } else if ("0000".equals(clampAlarmHex.replace(" ", ""))) {
                        clampAlarmPointModel.setValue("无报警");

                    }
                    Integer alarmDataStartAddress = ByteUtil.hexStringToNum(alarmDataStartAddressHex) + 1;
                    clampAlarmPointModel.setDataAddress(ByteUtil.numToLengthHexStr(alarmDataStartAddress, 4));
                    alarmPointList.add(clampAlarmPointModel);


                    pointModel.setDataMapHex(alarmDataMapHex);
                    pointModel.setPointList(alarmPointList);//添加子point


                    //CRC校验值
                    pointModel.setCRCHex(alarmResponseInstructionList.get(alarmResponseInstructionList.size() - 2) + alarmResponseInstructionList.get(alarmResponseInstructionList.size() - 1));

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
        sensorModel.setType(GlobalConstants.COM);
        List<PointModel> pointModelList = sensorModel.getPointModelList();
        for (PointModel pointModel : pointModelList) {
            switch (pointModel.getPointName()) {
                case GlobalConstants.COMPLEX_PROTECTION_TELEMETRY://XZL-801D微机综合保护测控装置-遥测
                    String requestInstruction = pointModel.getRequestInstruction();//01 03 00 00 00 0D 84 0F
                    String replace = requestInstruction.replace(" ", "");
                    if (StringUtils.isEmpty(replace)) {
                        return;
                    }
                    ArrayList<String> requestInstructionList = ByteUtil.strsToList(replace);
                    String dataStartAddressHex = requestInstructionList.get(2) + requestInstructionList.get(3);//数据起始地址16进制
                    pointModel.setDataStartAddress(dataStartAddressHex);//数据起始地址

                    String responseInstruction = pointModel.getResponseInstruction();

                    ArrayList<String> responseInstructionList = ByteUtil.strsToList(responseInstruction.replace(" ", ""));

                    if (responseInstructionList.size() < 1) {
                        return;
                    }
                    pointModel.setSlaveAddressHex(responseInstructionList.get(0));//从机地址
                    pointModel.setFunctionCodeHex(responseInstructionList.get(1));//功能码
                    String totalDataBytesHex = responseInstructionList.get(2);
                    pointModel.setTotalDataBytesHex(totalDataBytesHex);//数据字节总数


                    //解析16进制data
                    Integer totalDataBytes = ByteUtil.hexStringToNum(totalDataBytesHex);
                    int evenNumbers = totalDataBytes / 2;
                    LinkedHashMap<String, String> dataMapHex = new LinkedHashMap<>();

                    ArrayList<PointModel> pointList = new ArrayList<>();

                    int index = 3;
                    for (int i = 0; i < evenNumbers; i++) {
                        StringBuilder dataHex = new StringBuilder();
                        for (int j = 0; j < 2; j++) {
                            dataHex.append(responseInstructionList.get(index));
                            index++;
                        }
                        String valueHex = dataHex.toString();
                        dataMapHex.put(i + "", valueHex);

                        //解析
                        PointModel pointModel1 = new PointModel();


                        switch (i) {
                            case 0://测量A相电流
                                pointModel1.setPointName(GlobalConstants.MEASURING_PHASE_A_CURRENT);
                                pointModel1.setValue((ByteUtil.hexStringToNum(valueHex) * 6 / 4095) + "");
                                pointModel1.setUnit("A");
                                pointModel1.setDataAddress(ByteUtil.numToLengthHexStr(0, 4));
                                break;
                            case 1://测量B相电流
                                pointModel1.setPointName(GlobalConstants.MEASURING_PHASE_B_CURRENT);
                                pointModel1.setValue((ByteUtil.hexStringToNum(valueHex) * 6 / 4095) + "");
                                pointModel1.setUnit("A");
                                pointModel1.setDataAddress(ByteUtil.numToLengthHexStr(2, 4));
                                break;
                            case 2://测量C相电流
                                pointModel1.setPointName(GlobalConstants.MEASURING_C_PHASE_CURRENT);
                                pointModel1.setValue((ByteUtil.hexStringToNum(valueHex) * 6 / 4095) + "");
                                pointModel1.setUnit("A");
                                pointModel1.setDataAddress(ByteUtil.numToLengthHexStr(4, 4));
                                break;
                            case 3://A相电压
                                pointModel1.setPointName(GlobalConstants.PHASE_A_VOLTAGE);
                                pointModel1.setValue((ByteUtil.hexStringToNum(valueHex) * 120 / 4095) + "");
                                pointModel1.setUnit("V");
                                pointModel1.setDataAddress(ByteUtil.numToLengthHexStr(6, 4));
                                break;
                            case 4://B相电压
                                pointModel1.setPointName(GlobalConstants.B_PHASE_VOLTAGE);
                                pointModel1.setValue((ByteUtil.hexStringToNum(valueHex) * 120 / 4095) + "");
                                pointModel1.setUnit("V");
                                pointModel1.setDataAddress(ByteUtil.numToLengthHexStr(8, 4));
                                break;
                            case 5://C相电压
                                pointModel1.setPointName(GlobalConstants.C_PHASE_VOLTAGE);
                                pointModel1.setValue((ByteUtil.hexStringToNum(valueHex) * 120 / 4095) + "");
                                pointModel1.setUnit("V");
                                pointModel1.setDataAddress(ByteUtil.numToLengthHexStr(10, 4));
                                break;
                            case 6://AB相线电压
                                pointModel1.setPointName(GlobalConstants.AB_CAMERA_LINE_VOLTAGE);
                                pointModel1.setValue((ByteUtil.hexStringToNum(valueHex) * 120 / 4095) + "");
                                pointModel1.setUnit("V");
                                pointModel1.setDataAddress(ByteUtil.numToLengthHexStr(12, 4));
                                break;
                            case 7://BC相线电压
                                pointModel1.setPointName(GlobalConstants.BC_CAMERA_LINE_VOLTAGE);
                                pointModel1.setValue((ByteUtil.hexStringToNum(valueHex) * 120 / 4095) + "");
                                pointModel1.setUnit("V");
                                pointModel1.setDataAddress(ByteUtil.numToLengthHexStr(14, 4));
                                break;
                            case 8://CA相线电压
                                pointModel1.setPointName(GlobalConstants.CA_CAMERA_LINE_VOLTAGE);
                                pointModel1.setValue((ByteUtil.hexStringToNum(valueHex) * 120 / 4095) + "");
                                pointModel1.setUnit("V");
                                pointModel1.setDataAddress(ByteUtil.numToLengthHexStr(16, 4));
                                break;
                            case 9://有功功率P
                                pointModel1.setPointName(GlobalConstants.ACTIVE_POWER_P);
                                pointModel1.setValue((ByteUtil.hexStringToNum(valueHex) * 1247 / 4095) + "");
                                pointModel1.setUnit("W");
                                pointModel1.setDataAddress(ByteUtil.numToLengthHexStr(18, 4));
                                break;
                            case 10://无功功率Q
                                pointModel1.setPointName(GlobalConstants.REACTIVE_POWER_Q);
                                pointModel1.setValue((ByteUtil.hexStringToNum(valueHex) * 1247 / 4095) + "");
                                pointModel1.setUnit("W");
                                pointModel1.setDataAddress(ByteUtil.numToLengthHexStr(20, 4));
                                break;
                            case 11://功率因数
                                pointModel1.setPointName(GlobalConstants.POWER_FACTOR);
                                pointModel1.setValue((ByteUtil.hexStringToNum(valueHex) / 4095) + "");
                                pointModel1.setDataAddress(ByteUtil.numToLengthHexStr(22, 4));
                                break;
                            case 12://频率
                                pointModel1.setPointName(GlobalConstants.FREQUENCY);
                                pointModel1.setValue((ByteUtil.hexStringToNum(valueHex) * 60 / 4095) + "");
                                pointModel1.setUnit("HZ");
                                pointModel1.setDataAddress(ByteUtil.numToLengthHexStr(24, 4));
                                break;
                        }
                        pointList.add(pointModel1);

                    }
                    pointModel.setDataMapHex(dataMapHex);
                    pointModel.setPointList(pointList);//添加子point


                    //CRC校验值
                    pointModel.setCRCHex(responseInstructionList.get(responseInstructionList.size() - 2) + responseInstructionList.get(responseInstructionList.size() - 1));

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
                    if (alertTotalDataBytes != 0) {


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
                    }

                    //CRC校验值
                    pointModel.setCRCHex(alertResponseInstructionList.get(alertResponseInstructionList.size() - 2) + alertResponseInstructionList.get(alertResponseInstructionList.size() - 1));

                    pointModel.setTime(DateUtil.now());//设置时间
                    break;
            }
        }
    }
}
