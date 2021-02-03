package com.scdy.comprehensiveinsurance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scdy.comprehensiveinsurance.constant.GlobalConstants;
import com.scdy.comprehensiveinsurance.entity.ComEntity;
import com.scdy.comprehensiveinsurance.entity.EthEntity;
import com.scdy.comprehensiveinsurance.entity.SensorEntity;
import com.scdy.comprehensiveinsurance.model.PointModel;
import com.scdy.comprehensiveinsurance.model.SensorModel;
import com.scdy.comprehensiveinsurance.service.SensorDataService;
import com.scdy.comprehensiveinsurance.service.SensorService;
import com.scdy.comprehensiveinsurance.utils.ByteUtil;
import com.scdy.comprehensiveinsurance.utils.SendDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;

@Service
@Transactional
public class SensorDataServiceImpl implements SensorDataService {
    @Autowired
    private SensorService sensorService;

    @Override
    public SensorModel getSensorSendData(ComEntity comEntity) {
        SensorModel sensorModel = new SensorModel();

        //设置传感器模型id
        String sensorNameCode = comEntity.getSensorModel();
        QueryWrapper<SensorEntity> sensorEntityQueryWrapper1 = new QueryWrapper<>();
        sensorEntityQueryWrapper1.eq("is_deleted",0);
        sensorEntityQueryWrapper1.eq("sensor_name_code",sensorNameCode);
        SensorEntity sensorEntity = sensorService.getOne(sensorEntityQueryWrapper1);
        sensorModel.setSensorId(sensorEntity.getId());

        //设置传感器id
        sensorModel.setEthId(comEntity.getId());

        ArrayList<PointModel> pointModelList = new ArrayList<>();

        //设置传感器名称
        sensorModel.setSensorName(sensorEntity.getSensorName());

        //设置传感器编码
        sensorModel.setSensorNameId(sensorNameCode);

        switch (sensorNameCode) {
            case "1"://XZL-801D 微机综合保护测控装置
                String slaveAddressHex = ByteUtil.numToLengthHexStr(Integer.parseInt(comEntity.getDeviceAddress()),
                        2);//16进制从机地址
                //1、构建遥测请求指令 01 03 00 00 00 0D 84 0F
                String telemetryFunctionCodeHex = "03";//16进制功能码
                String telemetryDataStartAddressHex = "00 00";//16进制数据起始地址
                String telemetryDataLengthHex = "00 0D";//16进制数据长度
                String telemetrySendInstructionHex = SendDataUtil.getSendInstruction(slaveAddressHex, telemetryFunctionCodeHex,
                        telemetryDataStartAddressHex, telemetryDataLengthHex);
                PointModel telemetryPointModel = new PointModel();
                telemetryPointModel.setPointName(GlobalConstants.COMPLEX_PROTECTION_TELEMETRY);
                telemetryPointModel.setRequestInstruction(telemetrySendInstructionHex);
                pointModelList.add(telemetryPointModel);

                //2、构建警告请求指令 01 0C 00 25
                String alertFunctionCodeHex = "0C";//16进制功能码
                String alertSendInstructionHex = SendDataUtil.getSendInstruction(slaveAddressHex, alertFunctionCodeHex);
                PointModel alertSPointModel = new PointModel();
                alertSPointModel.setPointName(GlobalConstants.COMPLEX_PROTECTION_ALERT);
                alertSPointModel.setRequestInstruction(alertSendInstructionHex);
                pointModelList.add(alertSPointModel);
                break;
            case "3"://铁芯夹件

                String currentSlaveAddressHex = ByteUtil.numToLengthHexStr(Integer.parseInt(comEntity.getDeviceAddress()),
                        2);//16进制从机地址

                //1、读取时间和电流数据 01 03 00 00 00 0B 04 0D
                String currentFunctionCodeHex = "03";//16进制功能码
                String currentStartAddressHex = "00 00";//16进制数据起始地址
                String currentDataLengthHex = "00 0B";//16进制数据长度
                String currentSendInstructionHex = SendDataUtil.getSendInstruction(currentSlaveAddressHex, currentFunctionCodeHex,
                        currentStartAddressHex, currentDataLengthHex);
                PointModel currentPointModel = new PointModel();
                currentPointModel.setPointName(GlobalConstants.CORE_CLAMP_READ_TIME_CURRENT_DATA);
                currentPointModel.setRequestInstruction(currentSendInstructionHex);
                pointModelList.add(currentPointModel);

                //2、读报警标志 01 03 0F 00 00 02 C7 1F
                /*
                String alarmFunctionCodeHex = "03";//16进制功能码
                String alarmStartAddressHex = "0F 00";//16进制数据起始地址
                String alarmDataLengthHex = "00 02";//16进制数据长度
                String alarmSendInstructionHex = SendDataUtil.getSendInstruction(currentSlaveAddressHex, alarmFunctionCodeHex,
                        alarmStartAddressHex, alarmDataLengthHex);
                PointModel alarmPointModel = new PointModel();
                alarmPointModel.setPointName(GlobalConstants.CORE_CLAMP_ALARM_SIGN);
                alarmPointModel.setRequestInstruction(alarmSendInstructionHex);
                pointModelList.add(alarmPointModel);
                */
                break;
            case "5"://数采控制器

                String dataCollectionSlaveAddressHex = ByteUtil.numToLengthHexStr(Integer.parseInt(comEntity.getDeviceAddress()),
                        2);//16进制从机地址

                //1、继电器10路查询 01 01 00 00 00 0A BC 0D
                String dataCollectionFunctionCodeHex = "01";//16进制功能码
                String dataCollectionStartAddressHex = "00 00";//16进制数据起始地址
                String dataCollectionDataLengthHex = "00 0A";//16进制数据长度
                String dataCollectionSendInstructionHex = SendDataUtil.getSendInstruction(dataCollectionSlaveAddressHex, dataCollectionFunctionCodeHex,
                        dataCollectionStartAddressHex, dataCollectionDataLengthHex);
                PointModel dataCollectionPointModel = new PointModel();
                dataCollectionPointModel.setPointName(GlobalConstants.DATA_ACQUISITION_CONTROLLER_RELAY);
                dataCollectionPointModel.setRequestInstruction(dataCollectionSendInstructionHex);
                pointModelList.add(dataCollectionPointModel);

                //2、开关量12路查询 01 02 00 00 00 0C 78 0f
                String switchFunctionCodeHex = "02";//16进制功能码
                String switchStartAddressHex = "00 00";//16进制数据起始地址
                String switchDataLengthHex = "00 0C";//16进制数据长度
                String switchSendInstructionHex = SendDataUtil.getSendInstruction(dataCollectionSlaveAddressHex, switchFunctionCodeHex,
                        switchStartAddressHex, switchDataLengthHex);
                PointModel switchPointModel = new PointModel();
                switchPointModel.setPointName(GlobalConstants.DATA_ACQUISITION_CONTROLLER_SWITCH);
                switchPointModel.setRequestInstruction(switchSendInstructionHex);
                pointModelList.add(switchPointModel);
                break;
//            case "4"://温湿度
//
//                String temperatureHumiditySlaveAddressHex = ByteUtil.numToLengthHexStr(Integer.parseInt(comEntity.getDeviceAddress()),
//                        2);//16进制从机地址
//
//                //1、温湿度 02 04 04 0000 0001 E5 6B
//                String temperatureHumidityFunctionCodeHex = "04";//16进制功能码
//                String temperatureHumidityDataLengthHex = "04";//16进制数据长度
//                String temperatureHumidityStartAddressHex1 = "00 00";//16进制数据起始地址
//                String temperatureHumidityStartAddressHex2 = "00 01";//16进制数据起始地址
//                String temperatureHumiditySendInstructionHex = SendDataUtil.getSendInstruction(temperatureHumiditySlaveAddressHex, temperatureHumidityFunctionCodeHex,
//                        temperatureHumidityDataLengthHex,temperatureHumidityStartAddressHex1, temperatureHumidityStartAddressHex2);
//                PointModel temperatureHumidityPointModel = new PointModel();
//                temperatureHumidityPointModel.setPointName(GlobalConstants.ENVIRONMENTAL_CONTROL_TEMPERATURE_HUMIDITY);
//                temperatureHumidityPointModel.setRequestInstruction(temperatureHumiditySendInstructionHex);
//                pointModelList.add(temperatureHumidityPointModel);
//                break;

        }

        sensorModel.setPointModelList(pointModelList);
        return sensorModel;
    }

    @Override
    public SensorModel getSensorSendData(EthEntity ethEntity) {
        SensorModel sensorModel = new SensorModel();
        ArrayList<PointModel> pointModelList = new ArrayList<>();

        //sensor的id
        String sensorModelId = ethEntity.getSensorModel();//传感器型号id
        QueryWrapper<SensorEntity> sensorEntityQueryWrapper1 = new QueryWrapper<>();
        sensorEntityQueryWrapper1.eq("is_deleted",0);
        sensorEntityQueryWrapper1.eq("sensor_name_code",sensorModelId);
        SensorEntity sensorEntity = sensorService.getOne(sensorEntityQueryWrapper1);
        String sensorId = sensorEntity.getId();
        sensorModel.setSensorId(sensorId);

        //设置从机地址
        sensorModel.setDeviceAddress(ethEntity.getDeviceAddress());


        //设置deviceId
        String deviceId = ethEntity.getId();
        sensorModel.setEthId(deviceId);

        //设置传感器名称
        sensorModel.setSensorName(sensorEntity.getSensorName());

        //设置传感器编码
        sensorModel.setSensorNameId(sensorModelId);

        //设置tag
        sensorModel.setTag(ethEntity.getTag());

        //设置安装位置
        sensorModel.setInstallPostion(ethEntity.getInstallPostion());

        switch (sensorModelId) {
            case "4"://电子围栏
                //状态查询 FF 01 00 校验码
                String deviceAddressStr = ethEntity.getDeviceAddress();

                //传感器16进制地址
                String deviceAddressHex = getDeviceAddressHex(deviceAddressStr);

                //控制命令
                String controlCodeHex = "01";

                //数据长度
                String dataLengthHex = "00";

                String dataHex = deviceAddressHex + controlCodeHex + dataLengthHex;
                String dataWithBCC = ByteUtil.getDataHexWithBCC(dataHex);


                PointModel PointModel = new PointModel();
                PointModel.setPointName(GlobalConstants.ELECTRONIC_FENCE_STATUS_QUERY);
                PointModel.setRequestInstruction(dataWithBCC);
                pointModelList.add(PointModel);
                break;
            case "2"://火灾监控
                //读取主机信息

                // 读/写 十进制
                Integer readWriteDec = 0;
                // 数据长度
                String fireDataLengthHex = "00 02";
                // 功能码
                Integer functionCodeDec = 0;
                // 总线回路号
                Integer busLoopNumberDec = null;

                String direct = getDirect(readWriteDec, fireDataLengthHex, functionCodeDec, busLoopNumberDec);


                PointModel firePointModel = new PointModel();
                firePointModel.setPointName(GlobalConstants.HOST_INFORMATION);
                firePointModel.setRequestInstruction(direct);
                pointModelList.add(firePointModel);

                //2、构建警告请求指令 01 0C 00 25
//                 String alertFunctionCodeHex = "0C";//16进制功能码
//                 String alertSendInstructionHex = SendDataUtil.getSendInstruction(slaveAddressHex, alertFunctionCodeHex);
//                 PointModel alertSPointModel = new PointModel();
//                 alertSPointModel.setPointName(GlobalConstants.COMPLEX_PROTECTION_ALERT);
//                 alertSPointModel.setRequestInstruction(alertSendInstructionHex);
//                 pointModelList.add(alertSPointModel);
                break;
            case "6"://环控(利通)
                break;
            case "7"://蓄电池(利通)
//                String batteryDeviceAddressStr = ethEntity.getDeviceAddress();
//                String batteryDeviceAddressHex = getDeviceAddressHex(batteryDeviceAddressStr);//从机地址
//                String telemeteringFunctionCodeHex="03";//功能码

                //遥测0_79 如：01 03 00 00 00 50 CRC16
//                String telemetering_0_79_StartAddressHex="0000";//起始地址
//                String telemetering_0_79_DataLengthHex="0050";//数据长度
//                String telemetering_0_79_value=batteryDeviceAddressHex+telemeteringFunctionCodeHex+telemetering_0_79_StartAddressHex+telemetering_0_79_DataLengthHex;
//                String withCrc16 = SendDataUtil.getWithCrc16(telemetering_0_79_value);
//
//                PointModel telemetering_0_79_PointModel = new PointModel();
//                telemetering_0_79_PointModel.setPointName(GlobalConstants.BATTERY_LITONG_TELEMETERING_0_79);
//                telemetering_0_79_PointModel.setRequestInstruction(withCrc16);
//                pointModelList.add(telemetering_0_79_PointModel);

                //遥测1000_1207 如：01 03 03E8 00D0 CRC16
//                String telemetering_1000_1207_StartAddressHex="03E8";//起始地址
//                String telemetering_1000_1207_DataLengthHex="00D0";//数据长度
//                String telemetering_1000_1207_value=batteryDeviceAddressHex+telemeteringFunctionCodeHex+telemetering_1000_1207_StartAddressHex+telemetering_1000_1207_DataLengthHex;
//                String telemetering_1000_1207_withCrc16 = SendDataUtil.getWithCrc16(telemetering_1000_1207_value);
//
//                PointModel telemetering_1000_1207_PointModel = new PointModel();
//                telemetering_1000_1207_PointModel.setPointName(GlobalConstants.BATTERY_LITONG_TELEMETERING_1000_1207);
//                telemetering_1000_1207_PointModel.setRequestInstruction(telemetering_1000_1207_withCrc16);
//                pointModelList.add(telemetering_1000_1207_PointModel);

                //遥测1000_1207 如：01 03 03E8 00D0 CRC16
//                String telecommunicationFunctionCodeHex="01";//功能码
//                String telecommunicationStartAddressHex="0000";//起始地址
//                String telecommunicationDataLengthHex="00A0";//数据长度
//                String telecommunicationvalue=batteryDeviceAddressHex+telecommunicationFunctionCodeHex+telecommunicationStartAddressHex+telecommunicationDataLengthHex;
//                String telecommunicationWithCrc16 = SendDataUtil.getWithCrc16(telecommunicationvalue);
//
//                PointModel telecommunicationPointModel = new PointModel();
//                telecommunicationPointModel.setPointName(GlobalConstants.BATTERY_LITONG_TELECOMMUNICATION);
//                telecommunicationPointModel.setRequestInstruction(telecommunicationWithCrc16);
//                pointModelList.add(telecommunicationPointModel);

                break;
            case "8"://温湿度(利通环控)
                break;
            case "9"://照明(利通环控)
                break;
            case "10"://风机(利通环控)
                break;
            case "11"://烟感(利通环控)
                break;
            case "12"://存在探测(利通环控)
                break;
            case "13"://空调(利通环控)
                break;
            case "14"://峨山蓄电池
                String batterySlaveAddressHex = ByteUtil.numToLengthHexStr(Integer.parseInt(ethEntity.getDeviceAddress()), 2);//16进制从机地址

                //单体电压1-100#
                //11 03 00 03 00 64 B6 B1
                String functionCodeHex = "03";//16进制功能码
                String batteryDataLengthHex = "00 64";//16进制数据长度

                /*
                PointModel singleVoltagePointModel1_100 = new PointModel();
                singleVoltagePointModel1_100.setPointName(GlobalConstants.BATTERY_MOUNT_E_SINGLE_VOLTAGE_1_100);
                singleVoltagePointModel1_100.setRequestInstruction(SendDataUtil.getSendInstruction(batterySlaveAddressHex, functionCodeHex,
                        "00 03", batteryDataLengthHex));
                singleVoltagePointModel1_100.setSensorId(sensorId);
                singleVoltagePointModel1_100.setEthId(deviceId);
                pointModelList.add(singleVoltagePointModel1_100);


                //单体电压101-200#
                //11 03 00 67 00 64 F7 6E
                PointModel singleVoltagePointModel101_200 = new PointModel();
                singleVoltagePointModel101_200.setPointName(GlobalConstants.BATTERY_MOUNT_E_SINGLE_VOLTAGE_101_200);
                singleVoltagePointModel101_200.setRequestInstruction(SendDataUtil.getSendInstruction(batterySlaveAddressHex, functionCodeHex,
                        "00 67", batteryDataLengthHex));
                singleVoltagePointModel101_200.setSensorId(sensorId);
                singleVoltagePointModel101_200.setEthId(deviceId);
                pointModelList.add(singleVoltagePointModel101_200);

                //单体电压201-300#
                //11 03 00 CB 00 64 37 4F
                PointModel singleVoltagePointModel201_300 = new PointModel();
                singleVoltagePointModel201_300.setPointName(GlobalConstants.BATTERY_MOUNT_E_SINGLE_VOLTAGE_201_300);
                singleVoltagePointModel201_300.setRequestInstruction(SendDataUtil.getSendInstruction(batterySlaveAddressHex, functionCodeHex,
                        "00 CB", batteryDataLengthHex));
                singleVoltagePointModel201_300.setSensorId(sensorId);
                singleVoltagePointModel201_300.setEthId(deviceId);
                pointModelList.add(singleVoltagePointModel201_300);


                //单体内阻1-100#
                //11 03 01 32 00 04 E6 AA
                PointModel monomerResistancePointModel1_100 = new PointModel();
                monomerResistancePointModel1_100.setPointName(GlobalConstants.BATTERY_MOUNT_E_MONOMER_RESISTANCE_1_100);
                monomerResistancePointModel1_100.setRequestInstruction(SendDataUtil.getSendInstruction(batterySlaveAddressHex, functionCodeHex,
                        "01 32", batteryDataLengthHex));
                monomerResistancePointModel1_100.setSensorId(sensorId);
                monomerResistancePointModel1_100.setEthId(deviceId);
                pointModelList.add(monomerResistancePointModel1_100);

                //单体内阻101-200#
                //11 03 01 32 00 04 E6 AA
                PointModel monomerResistancePointModel101_200 = new PointModel();
                monomerResistancePointModel101_200.setPointName(GlobalConstants.BATTERY_MOUNT_E_MONOMER_RESISTANCE_101_200);
                monomerResistancePointModel101_200.setRequestInstruction(SendDataUtil.getSendInstruction(batterySlaveAddressHex, functionCodeHex,
                        "01 96", batteryDataLengthHex));
                monomerResistancePointModel101_200.setSensorId(sensorId);
                monomerResistancePointModel101_200.setEthId(deviceId);
                pointModelList.add(monomerResistancePointModel101_200);



                //单体内阻201-300#
                //11 03 01 32 00 04 E6 AA
                PointModel monomerResistancePointModel201_300 = new PointModel();
                monomerResistancePointModel201_300.setPointName(GlobalConstants.BATTERY_MOUNT_E_MONOMER_RESISTANCE_201_300);
                monomerResistancePointModel201_300.setRequestInstruction(SendDataUtil.getSendInstruction(batterySlaveAddressHex, functionCodeHex,
                        "01 FA", batteryDataLengthHex));
                monomerResistancePointModel201_300.setSensorId(sensorId);
                monomerResistancePointModel201_300.setEthId(deviceId);
                pointModelList.add(monomerResistancePointModel201_300);


                //电池温度1-100#
                //11 03 03 8D 00 04 D6 F6
                PointModel batteryTemperaturePointModel1_100 = new PointModel();
                batteryTemperaturePointModel1_100.setPointName(GlobalConstants.BATTERY_MOUNT_E_BATTERY_TEMPERATURE_1_100);
                batteryTemperaturePointModel1_100.setRequestInstruction(SendDataUtil.getSendInstruction(batterySlaveAddressHex, functionCodeHex,
                        "03 8D", batteryDataLengthHex));
                batteryTemperaturePointModel1_100.setSensorId(sensorId);
                batteryTemperaturePointModel1_100.setEthId(deviceId);
                pointModelList.add(batteryTemperaturePointModel1_100);


                //电池温度101-200#
                //11 03 03 8D 00 04 D6 F6
                PointModel batteryTemperaturePointModel101_200 = new PointModel();
                batteryTemperaturePointModel101_200.setPointName(GlobalConstants.BATTERY_MOUNT_E_BATTERY_TEMPERATURE_101_200);
                batteryTemperaturePointModel101_200.setRequestInstruction(SendDataUtil.getSendInstruction(batterySlaveAddressHex, functionCodeHex,
                        "03 F1", batteryDataLengthHex));
                batteryTemperaturePointModel101_200.setSensorId(sensorId);
                batteryTemperaturePointModel101_200.setEthId(deviceId);
                pointModelList.add(batteryTemperaturePointModel101_200);


                //电池温度201-300#
                //11 03 03 8D 00 04 D6 F6
                PointModel batteryTemperaturePointModel201_300 = new PointModel();
                batteryTemperaturePointModel201_300.setPointName(GlobalConstants.BATTERY_MOUNT_E_BATTERY_TEMPERATURE_201_300);
                batteryTemperaturePointModel201_300.setRequestInstruction(SendDataUtil.getSendInstruction(batterySlaveAddressHex, functionCodeHex,
                        "04 55", batteryDataLengthHex));
                batteryTemperaturePointModel201_300.setSensorId(sensorId);
                batteryTemperaturePointModel201_300.setEthId(deviceId);
                pointModelList.add(batteryTemperaturePointModel201_300);
*/

                //电池组总电压
                //11 03 07 18 00 01 06 2F
                PointModel totalVoltagePointModel = new PointModel();
                totalVoltagePointModel.setPointName(GlobalConstants.BATTERY_MOUNT_E_TOTAL_VOLTAGE);
                totalVoltagePointModel.setRequestInstruction(SendDataUtil.getSendInstruction(batterySlaveAddressHex, functionCodeHex,
                        "07 18", "00 01"));
                totalVoltagePointModel.setSensorId(sensorId);
                totalVoltagePointModel.setEthId(deviceId);
                pointModelList.add(totalVoltagePointModel);
                break;
        }

        sensorModel.setPointModelList(pointModelList);
        return sensorModel;
    }


    /**
     * 获取传感器16进制地址
     *
     * @param deviceAddressStr
     * @return
     */
    private String getDeviceAddressHex(String deviceAddressStr) {
        return ByteUtil.numToLengthHexStr(Integer.parseInt(deviceAddressStr), 2);
    }

    /**
     * 生成请求指令
     *
     * @param readWriteDec     读/写 十进制
     * @param dataLengthHex    数据长度 十六进制
     * @param functionCodeDec  功能码 十进制
     * @param busLoopNumberDec 总线回路号 十进制
     */
    private String getDirect(Integer readWriteDec, String dataLengthHex, Integer functionCodeDec, Integer busLoopNumberDec) {
        String readWriteHex = ByteUtil.numToLengthHexStr(readWriteDec, 2);
        String functionCodeHex = ByteUtil.numToLengthHexStr(functionCodeDec, 2);
        String busLoopNumberHex = null;
        if (!StringUtils.isEmpty(busLoopNumberDec)) {
            busLoopNumberHex = ByteUtil.numToLengthHexStr(busLoopNumberDec, 2);
        }

        String direct = null;
        if (StringUtils.isEmpty(busLoopNumberHex)) {
            direct = readWriteHex + dataLengthHex + functionCodeHex;
        } else {
            direct = readWriteHex + dataLengthHex + functionCodeHex + busLoopNumberHex;
        }

        return direct.replace(" ", "");

    }
}
