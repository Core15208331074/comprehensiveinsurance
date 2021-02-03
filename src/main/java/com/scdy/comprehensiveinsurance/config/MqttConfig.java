package com.scdy.comprehensiveinsurance.config;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.scdy.comprehensiveinsurance.entity.DeviceParamEntity;
import com.scdy.comprehensiveinsurance.entity.EthEntity;
import com.scdy.comprehensiveinsurance.entity.ParamEntity;
import com.scdy.comprehensiveinsurance.model.lighting.LightingModel;
import com.scdy.comprehensiveinsurance.model.request.datasDown.DatasResponseModel;
import com.scdy.comprehensiveinsurance.model.response.datasDown.DatasDownCmdInfoModel;
import com.scdy.comprehensiveinsurance.model.response.datasDown.DatasDownModel;
import com.scdy.comprehensiveinsurance.model.response.datasDown.DatasDownPacketModel;
import com.scdy.comprehensiveinsurance.service.*;
import com.scdy.comprehensiveinsurance.service.antiControl.EnvControlLitongService;
import com.scdy.comprehensiveinsurance.utils.DictUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import javax.annotation.Resource;

@Slf4j
@Configuration
@IntegrationComponentScan
public class MqttConfig {
    @Autowired
    private ConstructService constructService;
    @Autowired
    private DictUtil dictUtil;
    @Resource
    private MqttGatewayService mqttGatewayService;
    @Autowired
    private EnvControlLitongService envControlLitongService;
    @Autowired
    private ParamService paramService;
    @Autowired
    private DeviceParamService deviceParamService;
    @Autowired
    private EthService ethService;

    //    @Resource
//    private MqttGatewayService mqttGatewayService;
    @Value("${mqtt.username}")
    private String username;
    @Value("${mqtt.password}")
    private String password;
    @Value("${mqtt.host}")
    private String hostUrl;
    @Value("${mqtt.clientinid}")
    private String clientinid;
    @Value("${mqtt.clientoutid}")
    private String clientoutid;
    @Value("${mqtt.topic}")
    private String defaultTopic;
    @Value("${mqtt.timeout}")
    private int completionTimeout;


    /**
     * 配置MQTT消息推送
     */

    @Bean
    public MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setConnectionTimeout(10);
        mqttConnectOptions.setKeepAliveInterval(90);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());
        mqttConnectOptions.setServerURIs(new String[]{hostUrl});
        mqttConnectOptions.setKeepAliveInterval(2);
        return mqttConnectOptions;
    }


    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(getMqttConnectOptions());
        return factory;
    }


    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(clientoutid, mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(defaultTopic);
        return messageHandler;
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    /**
     * 接收MQTT传过来的信息
     */

    @Bean
    public MessageChannel onlineResponseInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer onlineResponseInbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(clientinid + defaultTopic,
                mqttClientFactory(),
                defaultTopic);
        adapter.setCompletionTimeout(completionTimeout);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(onlineResponseInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "onlineResponseInputChannel")
    public MessageHandler onlineResponseHandler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                try {
                    log.info("主题：{}，\r\n消息接收到的数据：{}", message.getHeaders().get("mqtt_receivedTopic"), message.getPayload());
                    DatasDownModel datasDownModel = JSONUtil.toBean(message.getPayload().toString(), DatasDownModel.class);
                    DatasDownPacketModel packet = datasDownModel.getPacket();
                    DatasDownCmdInfoModel cmd = packet.getCmd();
                    Integer type = cmd.getType();
                    DatasResponseModel datasResponseModel = null;
                    switch (type) {//0-心跳，1-查询,2-控制,3-获取当前巡检任务的工作情况（停止巡检，命令巡检，定时巡检）
                        case 0:
                            datasResponseModel = constructService.getHeartbeat("200", "心跳", "0");
                            break;
//                    case 1://根据设备id查询具体条数据
//                        String deviceId = cmd.getParamId();
//                        DatasResponseModel dataResponse = null;
//                        try {
//                            dataResponse = constructService.getDataResponse(deviceId);
//                            JSONObject jsonObject1 = JSONUtil.parseObj(dataResponse, false, true);
//                            mqttGatewayService.sendToMqtt(jsonObject1.toString(), "/v1/" + GlobalConstant.SERVER_ID + "/devices/datasDown/response");
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        break;
                        case 2:
                            String deviceParamId = cmd.getParamId();//参数id
                            DeviceParamEntity deviceParamEntity = deviceParamService.getById(deviceParamId);
                            ParamEntity paramServiceById = paramService.getById(deviceParamEntity.getParamId());
                            EthEntity ethServiceById = ethService.getById(deviceParamEntity.getDeviceId());



                            String dataType = paramServiceById.getDataType();
                            if ("控制".equals(dataType) || "遥控".equals(dataType)) {
                                String parameterCode = paramServiceById.getParameterCode();
                                switch (parameterCode) {
                                    case "LightSwtichControl"://照明控制
                                        String ethTag = ethServiceById.getTag();
                                        String paramTag = paramServiceById.getTag();
                                        int operation = cmd.getOperation();
                                        LightingModel lightingModel = new LightingModel();
                                        switch (ethTag) {
                                            case "1":
                                                switch (paramTag) {
                                                    case "1":
                                                        switch (operation) {
                                                            case 0:
                                                                lightingModel.setLightControl1_1(false);
                                                                break;
                                                            case 1:
                                                                lightingModel.setLightControl1_1(true);
                                                                break;
                                                        }
                                                        break;
                                                    case "2":
                                                        switch (operation) {
                                                            case 0:
                                                                lightingModel.setLightControl1_2(false);
                                                                break;
                                                            case 1:
                                                                lightingModel.setLightControl1_2(true);
                                                                break;
                                                        }
                                                        break;
                                                }
                                                break;
                                            case "2":
                                                switch (paramTag) {
                                                    case "1":
                                                        switch (operation) {
                                                            case 0:
                                                                lightingModel.setLightControl2_1(false);
                                                                break;
                                                            case 1:
                                                                lightingModel.setLightControl2_1(true);
                                                                break;
                                                        }
                                                        break;
                                                    case "2":
                                                        switch (operation) {
                                                            case 0:
                                                                lightingModel.setLightControl2_2(false);
                                                                break;
                                                            case 1:
                                                                lightingModel.setLightControl2_2(true);
                                                                break;
                                                        }
                                                        break;
                                                }
                                                break;
                                        }


                                        ThreadUtil.execAsync(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    envControlLitongService.lightingOperation(lightingModel);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });

                                        datasResponseModel = constructService.getHeartbeat("300", "照明控制", "2");
                                        break;
                                }
                            }
                            break;
                    }
                    JSONObject jsonObject = JSONUtil.parseObj(datasResponseModel, false, true);
                    mqttGatewayService.sendToMqtt(jsonObject.toString(), "/v1/" + dictUtil.getDictValue("mqtt", "server_id") + "/devices/datasDown/response");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}