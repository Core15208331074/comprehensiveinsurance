package com.scdy.comprehensiveinsurance.drive.tcp.client;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scdy.comprehensiveinsurance.entity.EthEntity;
import com.scdy.comprehensiveinsurance.entity.SensorEntity;
import com.scdy.comprehensiveinsurance.model.PointModel;
import com.scdy.comprehensiveinsurance.model.SensorModel;
import com.scdy.comprehensiveinsurance.model.request.datasUp.DataUpPacketModel;
import com.scdy.comprehensiveinsurance.model.request.datasUp.DatasUpModel;
import com.scdy.comprehensiveinsurance.model.request.datasUp.DatasUpParamDataModel;
import com.scdy.comprehensiveinsurance.service.*;
import com.scdy.comprehensiveinsurance.service.analysis.impl.EthProtocolAnalysisServiceImpl;
import com.scdy.comprehensiveinsurance.utils.DictUtil;
import com.scdy.comprehensiveinsurance.utils.SpringUtil;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Data
public class TcpClientHandler extends ChannelHandlerAdapter {
    private ConstructService constructService = SpringUtil.getBean(ConstructService.class);
    private DictUtil dictUtil = SpringUtil.getBean(DictUtil.class);
    private MqttGatewayService mqttGatewayService = SpringUtil.getBean(MqttGatewayService.class);
    private EthService ethService = SpringUtil.getBean(EthService.class);
    private EthEntity ethEntity;
    private SensorModel sensorModel;

    public TcpClientHandler(EthEntity ethEntity, SensorModel sensorModel) {
        this.ethEntity = ethEntity;
        this.sensorModel = sensorModel;
    }

    private StringBuffer stringBuffer = new StringBuffer();//用于接收存储串口发过来的数据 必须用static才能接收到到数据 但是可能出现其他问题，多线程字段共享


    /**
     * 客户端接收到数据的回调
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String str = msg.toString();


        SensorService sensorService = SpringUtil.getBean(SensorService.class);
        QueryWrapper<SensorEntity> sensorEntityQueryWrapper = new QueryWrapper<>();
        sensorEntityQueryWrapper.eq("is_deleted", 0);
        sensorEntityQueryWrapper.eq("type", "eth");
        sensorEntityQueryWrapper.eq("sensor_name_code", ethEntity.getSensorModel());
        SensorEntity sensorEntity = sensorService.getOne(sensorEntityQueryWrapper);

        //如果是反控必须设置为字节接收信息返回自己处理
        String isControl = sensorModel.getIsControl();
        if ("1".equals(isControl)) {
            sensorEntity.setIsSend("0");
        }

        switch (sensorEntity.getIsSend()) {
            case "0"://表示接收返回信息自己处理
                stringBuffer.append(str);
                break;
            case "1"://表示本类处理
                //设置返回的指令
                List<PointModel> pointModelList = sensorModel.getPointModelList();
                pointModelList.clear();//清空集合里面之前解析的
                PointModel pointModel = new PointModel();
                pointModel.setResponseInstruction(str);
                pointModelList.add(pointModel);

                //解析
                EthProtocolAnalysisServiceImpl protocolAnalysisService = SpringUtil.getBean(EthProtocolAnalysisServiceImpl.class);
                protocolAnalysisService.analysis(sensorModel);

                //4、保持到时序数据库
                InfluxdbService influxdbService = SpringUtil.getBean(InfluxdbService.class);
                influxdbService.saveEthToInfluxdb(sensorModel);


                //TODO 根据实际情况拼接参数发送到MQTT  不为反控就发送到mqtt
                String isControl1 = sensorModel.getIsControl();
                if (!"1".equals(isControl1)) {

                    boolean flag = true;
                    List<PointModel> pointModelList1 = sensorModel.getPointModelList();
                    for (PointModel model : pointModelList1) {
                        String responseInstruction = model.getResponseInstruction();
                        if (StringUtils.isEmpty(responseInstruction)) {
                            flag = false;
                        }
                    }

                    if (flag) {
//                        MqttGatewayService mqttGatewayService = SpringUtil.getBean(MqttGatewayService.class);
//                        String data = JSONUtil.parseObj(sensorModel, false, true).toString();
//                        String comprehensiveinsurance = "comprehensiveinsurance";
//                        mqttGatewayService.sendToMqtt(data, comprehensiveinsurance);
//                        log.info("发送到mqtt的主题：" + comprehensiveinsurance + "\r\n数据：" + data);




                        DatasUpModel datasUp = constructService.getDatasUp(sensorModel);

                        String data = JSONUtil.parseObj(datasUp, false, true).toString();

                        String topic = "/v1/" + dictUtil.getDictValue("mqtt", "server_id") + "/devices/datas/up";
                        mqttGatewayService.sendToMqtt(data, topic);
                        log.info("发送到mqtt\r\n名称:{}\r\n主题:{}\r\n数据：{}", "数据上传", topic, data);


                        //更新设备时间，用于设备是否在线监测
                        JSONObject packet1 = datasUp.getPacket();
                        DataUpPacketModel packet = JSONUtil.toBean(packet1, DataUpPacketModel.class);
                        List<DatasUpParamDataModel> datas = packet.getDatas();
                        for (DatasUpParamDataModel datasUpParamDataModel : datas) {
                            String deviceId = datasUpParamDataModel.getDeviceId();
                            EthEntity ethEntity = ethService.getById(deviceId);
                            ethEntity.setUpdateTime(DateUtil.toLocalDateTime(DateUtil.dateSecond()));
                            ethService.updateById(ethEntity);
                        }
                    }

                }

                break;
            case "2"://表示返回去自己处理，同时还要不定时接收信息
                break;
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

}
