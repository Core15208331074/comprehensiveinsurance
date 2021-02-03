package com.scdy.comprehensiveinsurance.drive.tcp.server;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scdy.comprehensiveinsurance.constant.GlobalConstants;
import com.scdy.comprehensiveinsurance.entity.EthEntity;
import com.scdy.comprehensiveinsurance.entity.SensorEntity;
import com.scdy.comprehensiveinsurance.model.PointModel;
import com.scdy.comprehensiveinsurance.model.SensorModel;
import com.scdy.comprehensiveinsurance.service.InfluxdbService;
import com.scdy.comprehensiveinsurance.service.MqttGatewayService;
import com.scdy.comprehensiveinsurance.service.SensorService;
import com.scdy.comprehensiveinsurance.service.analysis.impl.EthProtocolAnalysisServiceImpl;
import com.scdy.comprehensiveinsurance.utils.ByteUtil;
import com.scdy.comprehensiveinsurance.utils.SpringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * tcp server操作
 */
@Slf4j
public class TcpServerOperation {
    private EthEntity ethEntity;
    private SensorModel sensorModel;
//    private boolean flag;


    public TcpServerOperation(EthEntity ethEntity, SensorModel sensorModel) {
        this.ethEntity = ethEntity;
        this.sensorModel = sensorModel;


    }

    /**
     * 创建tcp client
     *
     * @return
     * @throws Exception
     */
    public synchronized TcpServer creatTcpServer() throws Exception {
        String localPortStr = ethEntity.getLocalPort();
        Integer localPort = Integer.parseInt(localPortStr);

        //1、创建tcp server连接
        String key = localPortStr;
        TcpServer tcpServer;
        synchronized (key) {
            Map<String, TcpServer> tcpServerMap = GlobalConstants.getTcpServerMap();

            tcpServer = tcpServerMap.get(key);

            //1、如果tcp server不存在则创建
            if (StringUtils.isEmpty(tcpServer)) {
                tcpServer = new TcpServer(localPort, ethEntity, sensorModel);
            }

            //2、如果tcp client失效则创建
            ChannelFuture channelFuture = tcpServer.getChannelFuture();
            if (!channelFuture.isSuccess() || !channelFuture.channel().isActive() || !channelFuture.channel().isOpen() || !channelFuture.channel().isWritable()) {
                tcpServer = new TcpServer(localPort, ethEntity, sensorModel);
            }
        }

        return tcpServer;
    }

    /**
     * 发送和接收数据
     *
     * @param tcpServer
     * @param channelHandlerContext
     * @param msg
     * @param isControl
     */
    public String sendMsg(TcpServer tcpServer, ChannelHandlerContext channelHandlerContext, String msg, String isControl) throws Exception {
        //发送数据到tcp服务端
        ByteBuf buffer = channelHandlerContext.channel().alloc().buffer();
        ByteBuf byteBuf = buffer.writeBytes(ByteUtil.strToBytes(msg));//或者ByteUtil.hexStringToBytes(hexMsgStr)
        channelHandlerContext.channel().writeAndFlush(byteBuf).sync();//必须是同步发送，不然可能导致数据还没发送成功线程就结束了
        //获取返回来的数据
        String result = null;
        if (!"1".equals(isControl)) {//非控制才接收数据
            result = tcpServer.getTcpServerHandler().receiveMsg();
        }

        return result;
    }

    public SensorModel startRun() throws Exception {
        //1、获取tcp client
        Boolean isStart = ethEntity.getIsStart();
        if (isStart) {

            //创建tcp server
            TcpServer tcpServer = creatTcpServer();


            SensorService sensorService = SpringUtil.getBean(SensorService.class);
            QueryWrapper<SensorEntity> sensorEntityQueryWrapper = new QueryWrapper<>();
            sensorEntityQueryWrapper.eq("is_deleted", 0);
            sensorEntityQueryWrapper.eq("type", "eth");
            sensorEntityQueryWrapper.eq("sensor_name_code", ethEntity.getSensorModel());
            SensorEntity sensorEntity = sensorService.getOne(sensorEntityQueryWrapper);

            //如果是反控，必须设置为主动发送信息
            String isControl = sensorModel.getIsControl();
            if ("1".equals(isControl)) {
                sensorEntity.setIsSend("0");
            }

            switch (sensorEntity.getIsSend()) {
                case "0"://表示主动发送请求指令
                    Map<String, ChannelHandlerContext> tcpClientChannelMap = GlobalConstants.getTcpClientChannelMap();
                    for (Map.Entry<String, ChannelHandlerContext> tcpClientChannelEntry : tcpClientChannelMap.entrySet()) {
                        ChannelHandlerContext channelHandlerContext = tcpClientChannelEntry.getValue();


                        //2、发送和接收数据
                        List<PointModel> pointModelList = sensorModel.getPointModelList();
                        for (PointModel pointModel : pointModelList) {
                            String requestInstruction = pointModel.getRequestInstruction();//请求指令
                            log.info(sensorModel.getSensorName() + "_" + pointModel.getPointName() + "请求指令:{}", requestInstruction);
                            String responseInstruction = sendMsg(tcpServer, channelHandlerContext, requestInstruction, isControl);
                            pointModel.setResponseInstruction(responseInstruction);
                            log.info(sensorModel.getSensorName() + "_" + pointModel.getPointName() + "接收指令:{}", responseInstruction);
                       Thread.sleep(8000);
                        }

                        if (!"1".equals(isControl)) {//非控制执行下面语句
                            //3、解析
                            EthProtocolAnalysisServiceImpl protocolAnalysisService = SpringUtil.getBean(EthProtocolAnalysisServiceImpl.class);
                            protocolAnalysisService.analysis(sensorModel);

                            //4、保持到时序数据库
                            InfluxdbService influxdbService = SpringUtil.getBean(InfluxdbService.class);
                            influxdbService.saveEthToInfluxdb(sensorModel);

                            //TODO 根据实际情况拼接参数发送到MQTT  不为反控就发送到mqtt
                            boolean flag = true;
                            List<PointModel> pointModelList1 = sensorModel.getPointModelList();
                            for (PointModel pointModel : pointModelList1) {
                                String responseInstruction = pointModel.getResponseInstruction();
                                if (StringUtils.isEmpty(responseInstruction)) {
                                    flag = false;
                                }
                            }

                            if (flag) {
                                MqttGatewayService mqttGatewayService = SpringUtil.getBean(MqttGatewayService.class);
                                String data = JSONUtil.parseObj(sensorModel, false, true).toString();
                                String topic = "comprehensiveinsurance";
                                mqttGatewayService.sendToMqtt(data, topic);
                                log.info("发送到mqtt\r\n名称:{}\r\n主题:{}\r\n数据：{}", "数据上传", topic, data);
                            }
                        }

                    }


                    break;
                case "1"://表示不主动发送请求指令
                    break;
                case "2"://主动发送请求指令，并且也需要不定时自动接收返回指令
                    break;
            }
        }

        return sensorModel;
    }


}
