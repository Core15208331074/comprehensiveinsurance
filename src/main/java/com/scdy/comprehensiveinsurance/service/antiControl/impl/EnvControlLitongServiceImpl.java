package com.scdy.comprehensiveinsurance.service.antiControl.impl;

import com.scdy.comprehensiveinsurance.constant.GlobalConstants;
import com.scdy.comprehensiveinsurance.drive.tcp.server.TcpServerOperation;
import com.scdy.comprehensiveinsurance.entity.EthEntity;
import com.scdy.comprehensiveinsurance.model.PointModel;
import com.scdy.comprehensiveinsurance.model.SensorModel;
import com.scdy.comprehensiveinsurance.model.lighting.LightingModel;
import com.scdy.comprehensiveinsurance.service.antiControl.EnvControlLitongService;
import com.scdy.comprehensiveinsurance.utils.DictUtil;
import com.scdy.comprehensiveinsurance.utils.SendDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


@Service
@Transactional
public class EnvControlLitongServiceImpl implements EnvControlLitongService {
    @Autowired
    private DictUtil dictUtil;

    @Override
    public void lightingOperation(LightingModel lightingModel) throws Exception {
        String localPort = dictUtil.getDictValue("sys", "environmental_control_tcp_server_local_port");//环控tcp server端口
        lightingModel.setLocalPort(localPort);
        //1、构建控制指令
        LinkedHashMap<String, String> lightingControlMap = getLightingControlCode(lightingModel);
        //2、发送控制指令

        EthEntity ethEntity = new EthEntity();
        ethEntity.setLocalPort(lightingModel.getLocalPort());
        ethEntity.setSensorModel("9");//照明(利通环控照明)
        ethEntity.setIsStart(true);

        SensorModel sensorModel = new SensorModel();
        sensorModel.setIsControl("1");//反控
        sensorModel.setSensorNameId("9");
        sensorModel.setSensorName(GlobalConstants.ENVIRONMENTAL_CONTROL_LITONG_LIGHTING_CONTROL);
        ArrayList<PointModel> pointModels = new ArrayList<>();


        for (Map.Entry<String, String> entry : lightingControlMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            PointModel pointModel = new PointModel();
            pointModel.setPointName(key);
            pointModel.setRequestInstruction(value);
            pointModels.add(pointModel);
        }
        sensorModel.setPointModelList(pointModels);
        TcpServerOperation tcpServerOperation = new TcpServerOperation(ethEntity, sensorModel);
        SensorModel sensorModel1 = tcpServerOperation.startRun();
    }

    /**
     * 构建照明控制指令
     *
     * @param lightingModel
     * @return
     */
    private LinkedHashMap<String, String> getLightingControlCode(LightingModel lightingModel) {
        LinkedHashMap<String, String> instructionsMap = new LinkedHashMap<>();

        //1、构建控制指令
        Boolean lightControl1_1 = lightingModel.getLightControl1_1();
        Boolean lightControl1_2 = lightingModel.getLightControl1_2();
        Boolean lightControl2_1 = lightingModel.getLightControl2_1();
        Boolean lightControl2_2 = lightingModel.getLightControl2_2();


        String slaveAaddressHex = "06";
        String functionCodeHex = "06";

        //灯控1-1
        if (!StringUtils.isEmpty(lightControl1_1) && true == lightControl1_1) {
            String instructionsHex = "00 3D 00 01";
            instructionsMap.put("1_1_true", getInstructions(slaveAaddressHex, functionCodeHex, instructionsHex));
        } else if (!StringUtils.isEmpty(lightControl1_1) && false == lightControl1_1) {
            String instructionsHex = "00 3D 00 00";
            instructionsMap.put("1_1_false", getInstructions(slaveAaddressHex, functionCodeHex, instructionsHex));
        }

        //灯控1-2
        if (!StringUtils.isEmpty(lightControl1_2) && true == lightControl1_2) {
            String instructionsHex = "00 3E 00 01";
            instructionsMap.put("1_2_true", getInstructions(slaveAaddressHex, functionCodeHex, instructionsHex));
        } else if (!StringUtils.isEmpty(lightControl1_2) && false == lightControl1_2) {
            String instructionsHex = "00 3E 00 00";
            instructionsMap.put("1_2_false", getInstructions(slaveAaddressHex, functionCodeHex, instructionsHex));
        }

        //灯控2-1
        if (!StringUtils.isEmpty(lightControl2_1) && true == lightControl2_1) {
            String instructionsHex = "00 3F 00 01";
            instructionsMap.put("2_1_true", getInstructions(slaveAaddressHex, functionCodeHex, instructionsHex));
        } else if (!StringUtils.isEmpty(lightControl2_1) && false == lightControl2_1) {
            String instructionsHex = "00 3F 00 00";
            instructionsMap.put("2_1_false", getInstructions(slaveAaddressHex, functionCodeHex, instructionsHex));
        }

        //灯控2-2
        if (!StringUtils.isEmpty(lightControl2_2) && true == lightControl2_2) {
            String instructionsHex = "00 40 00 01";
            instructionsMap.put("2_2_true", getInstructions(slaveAaddressHex, functionCodeHex, instructionsHex));
        } else if (!StringUtils.isEmpty(lightControl2_2) && false == lightControl2_2) {
            String instructionsHex = "00 40 00 00";
            instructionsMap.put("2_2_false", getInstructions(slaveAaddressHex, functionCodeHex, instructionsHex));
        }

        return instructionsMap;
    }


    /**
     * @param slaveAaddressHex 串口地址
     * @param functionCodeHex  功能码
     * @param instructionsHex  指令
     * @return
     */
    private String getInstructions(String slaveAaddressHex, String functionCodeHex, String instructionsHex) {
//        06 06 00 47 00 00 38 68
        String value = slaveAaddressHex + functionCodeHex + instructionsHex;
        String withCrc16 = SendDataUtil.getWithCrc16(value);
        return withCrc16;
    }
}
