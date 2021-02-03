package com.scdy.comprehensiveinsurance.service.antiControl.impl;

import com.scdy.comprehensiveinsurance.constant.GlobalConstants;
import com.scdy.comprehensiveinsurance.drive.tcp.client.TcpClientOperation;
import com.scdy.comprehensiveinsurance.entity.EthEntity;
import com.scdy.comprehensiveinsurance.model.PointModel;
import com.scdy.comprehensiveinsurance.model.SensorModel;
import com.scdy.comprehensiveinsurance.model.electronicFence.ClothRemovalSetModel;
import com.scdy.comprehensiveinsurance.service.antiControl.ElectronicFenceService;
import com.scdy.comprehensiveinsurance.utils.ByteUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;


@Service
@Transactional
public class ElectronicFenceServiceImpl implements ElectronicFenceService {
    @Override
    public SensorModel clothRemovalSet(ClothRemovalSetModel clothRemovalSetModel) throws Exception {
        //构建请求指令
        String clothRemovalSetRequestCode = getClothRemovalSetRequestCode(clothRemovalSetModel.getDefenseZoneNumberHex(), clothRemovalSetModel.getClothRemovalMarkHex());

        EthEntity ethEntity = new EthEntity();
        ethEntity.setTargetIp(clothRemovalSetModel.getServerIp());
        ethEntity.setTargetPort(clothRemovalSetModel.getPort());
        ethEntity.setSensorModel("1");//电子围栏

        SensorModel sensorModel = new SensorModel();
        sensorModel.setIsControl("1");//反控
        sensorModel.setSensorNameId("1");
        sensorModel.setSensorName(GlobalConstants.ELECTRONIC_FENCE);
        ArrayList<PointModel> pointModels = new ArrayList<>();
        PointModel pointModel = new PointModel();
        pointModel.setPointName(GlobalConstants.ELECTRONIC_FENCE_CLOTH_REMOVAL_CONTROL);
        pointModel.setRequestInstruction(clothRemovalSetRequestCode);
        pointModels.add(pointModel);
        sensorModel.setPointModelList(pointModels);

        TcpClientOperation tcpClientOperation = new TcpClientOperation(ethEntity, sensorModel);
        SensorModel sensorModel1 = tcpClientOperation.startRun();
        return sensorModel1;
    }

    /**
     * 构建布撤防设置请求指令
     *
     * @param defenseZoneNumberHex 防区编号
     * @param clothRemovalMark     布撤防标志  01=布防，00=撤防
     * @return
     */
    private String getClothRemovalSetRequestCode(String defenseZoneNumberHex, String clothRemovalMark) {
        String startAddressHex = "FF";//起始地址
        String controlCodeHex = "02";//控制命令
        String dataLengthHex = "02";//数据长度
        String codeHex = startAddressHex + controlCodeHex + dataLengthHex + defenseZoneNumberHex + clothRemovalMark;
        String dataHexWithBCC = ByteUtil.getDataHexWithBCC(codeHex);
        return dataHexWithBCC;
    }
}
