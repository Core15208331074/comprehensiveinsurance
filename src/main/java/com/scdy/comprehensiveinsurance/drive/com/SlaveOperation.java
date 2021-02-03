package com.scdy.comprehensiveinsurance.drive.com;

import cn.hutool.json.JSONUtil;
import com.scdy.comprehensiveinsurance.constant.GlobalConstants;
import com.scdy.comprehensiveinsurance.entity.ComEntity;
import com.scdy.comprehensiveinsurance.model.PointModel;
import com.scdy.comprehensiveinsurance.model.SensorModel;
import com.scdy.comprehensiveinsurance.service.InfluxdbService;
import com.scdy.comprehensiveinsurance.service.MqttGatewayService;
import com.scdy.comprehensiveinsurance.service.analysis.impl.ComProtocolAnalysisServiceImpl;
import com.scdy.comprehensiveinsurance.utils.ByteUtil;
import com.scdy.comprehensiveinsurance.utils.SpringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.rxtx.RxtxChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
public class SlaveOperation {

    public ComEntity comEntity;//串口信息
    private SensorModel sensorModel;//发送信息


    public SlaveOperation(ComEntity comEntity, SensorModel sensorModel) {
        this.sensorModel = sensorModel;
        this.comEntity = comEntity;
    }


    /**
     * 创建COM连接
     *
     * @return
     * @throws Exception
     */
    public ComSlave creatComSlave() throws Exception {
        String serialPortName = comEntity.getSerialPortName();//串口名称
        Integer baudRate = comEntity.getBaudRate();//波特率
        String checkDigit = comEntity.getCheckDigit();//校验码
        String dataBit = comEntity.getDataBit();//数据位
        String stopBit = comEntity.getStopBit();//停止位

        //1、创建tcp client连接
        Map<String, ComSlave> comSlaveMap = GlobalConstants.getComSlaveMap();
        String key = serialPortName + ":" + baudRate + ":" + checkDigit + ":" + dataBit + ":" + stopBit;
        ComSlave comSlave = comSlaveMap.get(key);

        //1、如果comSlave不存在则创建
        if (StringUtils.isEmpty(comSlave)) {
            comSlave = new ComSlave(serialPortName, baudRate, checkDigit, dataBit, stopBit);
        }

        //2、如果comSlave失效则创建
        RxtxChannel rxtxChannel = comSlave.getRxtxChannel();
        if (!rxtxChannel.isActive() || !rxtxChannel.isOpen() || !rxtxChannel.isWritable()) {
            comSlave = new ComSlave(serialPortName, baudRate, checkDigit, dataBit, stopBit);
        }

        return comSlave;
    }

    /**
     * 发送和接收数据
     *
     * @param comSlave
     * @param msg
     */
    public String sendMsg(ComSlave comSlave, String msg) throws Exception {
        //发送数据到串口
        RxtxChannel rxtxChannel = comSlave.getRxtxChannel();

        if (!rxtxChannel.isActive() || !rxtxChannel.isOpen() || !rxtxChannel.isWritable()) {
            comSlave = creatComSlave();
            rxtxChannel = comSlave.getRxtxChannel();
        }


        ByteBuf buffer = rxtxChannel.alloc().buffer();
        ByteBuf byteBuf = buffer.writeBytes(ByteUtil.strToBytes(msg));//或者ByteUtil.hexStringToBytes(hexMsgStr)
        rxtxChannel.writeAndFlush(byteBuf).sync();//必须是同步发送，不然可能导致数据还没发送成功线程就结束了


        //获取返回来的数据
        String result = comSlave.getRxtxHandler().receiveMsg();
        return result;
    }

    /**
     * 发送数据并且获取返回数据
     */
    public SensorModel startRun() throws Exception {
        //1、创建com slave
        ComSlave comSlave = creatComSlave();

        //2、发送和接收数据
        List<PointModel> pointModelList = sensorModel.getPointModelList();
        for (PointModel pointModel : pointModelList) {
            String requestInstruction = pointModel.getRequestInstruction();//请求指令
            log.info(sensorModel.getSensorName() + "_" + pointModel.getPointName() + "请求指令:{}", requestInstruction);
            String responseInstruction = sendMsg(comSlave, requestInstruction);
            pointModel.setResponseInstruction(responseInstruction);
            log.info(sensorModel.getSensorName() + "_" + pointModel.getPointName() + "接收指令:{}", responseInstruction);
        }

        //3、解析
        ComProtocolAnalysisServiceImpl protocolAnalysisService = SpringUtil.getBean(ComProtocolAnalysisServiceImpl.class);
        protocolAnalysisService.analysis(sensorModel);


        //4、保持到时序数据库
        InfluxdbService influxdbService = SpringUtil.getBean(InfluxdbService.class);
        influxdbService.saveEthToInfluxdb(sensorModel);
//        //如果请求指令是空则不存入时序数据库
//        for (PointModel pointModel : pointModelList) {
//            String responseInstruction = pointModel.getResponseInstruction();
//            if (StringUtils.isEmpty(responseInstruction)) {
//                return sensorModel;
//            }
//        }
//
//        //4、存储数据到influxDB
//        Point.Builder builder = Point.measurement("sensorModel");
//
//        if (!StringUtils.isEmpty(sensorModel.getIsControl())) {
//            builder.tag("isControl", sensorModel.getIsControl());
//        } else {
//            builder.tag("isControl", "0");//0表示查询，1表示反控
//        }
//
//        builder.tag("sensorNameId", sensorModel.getSensorNameId());
//        builder.tag("sensorName", sensorModel.getSensorName());
//        builder.tag("type", sensorModel.getType());//表示串口类型 GlobalConstants.COM
//
//        builder.addField("json", JSONUtil.parseObj(sensorModel).toString());
//        Point point = builder.build();
//        InfluxDB influxDB = SpringUtil.getBean(InfluxDB.class);
//        Environment environment = SpringUtil.getBean(Environment.class);
//        influxDB.setDatabase(environment.getProperty("spring.influx.database"));
//        if (!influxDB.databaseExists(environment.getProperty("spring.influx.database"))) {//数据库不存在
//            //创建数据库
//            influxDB.createDatabase(environment.getProperty("spring.influx.database"));
//            //创建存储策略，存储30天自动删除  rpName,  database,  duration,  replicationFactor,  isDefault
//            influxDB.createRetentionPolicy(environment.getProperty("spring.influx.rpName"), environment.getProperty("spring.influx.database"), environment.getProperty("spring.influx.duration"), Integer.parseInt(environment.getProperty("spring.influx.replicationFactor")), true);
//        }
//        influxDB.setRetentionPolicy(environment.getProperty("spring.influx.rpName"));
//        influxDB.write(point);
//            Thread.sleep(5000);
        //TODO 根据实际情况拼接参数发送到MQTT
        MqttGatewayService mqttGatewayService = SpringUtil.getBean(MqttGatewayService.class);
        mqttGatewayService.sendToMqtt(JSONUtil.parseObj(sensorModel, false, true).toString(), "comprehensiveinsurance");


        return sensorModel;
    }


}

