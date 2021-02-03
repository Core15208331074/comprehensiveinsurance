package com.scdy.comprehensiveinsurance.task;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.scdy.comprehensiveinsurance.model.request.deviceInfoUp.DeviceInfoUpModel;
import com.scdy.comprehensiveinsurance.model.request.offlineUp.OfflineModel;
import com.scdy.comprehensiveinsurance.model.request.onlineUp.OnlineModel;
import com.scdy.comprehensiveinsurance.service.ConstructService;
import com.scdy.comprehensiveinsurance.service.EthService;
import com.scdy.comprehensiveinsurance.service.MqttGatewayService;
import com.scdy.comprehensiveinsurance.utils.DictUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class AppApplicationListener implements ApplicationListener {
    @Autowired
    private ConstructService constructService;
    @Autowired
    private DictUtil dictUtil;
    @Autowired
    private EthService ethService;
    @Resource
    MqttGatewayService mqttGatewayService;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {//项目启动

/*
            //启动环控
            ThreadUtil.execAsync(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(10000);
                        ethService.isStartById("247f0dd1e571ebf29657a13225e3b6ba",true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            //启动蓄电池
            ThreadUtil.execAsync(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(10000);
                        ethService.isStartById("b35980342207f7779b4a73fe8f643add",true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            */


            //发送上线通知到MQTT
            ThreadUtil.execAsync(new Runnable() {
                @Override
                public void run() {

                    OnlineModel online = constructService.getOnline();
                    JSONObject jsonObject = JSONUtil.parseObj(online, false, true);
                    String topic = "/v1/services/online/up";
                    String data = jsonObject.toString();
                    mqttGatewayService.sendToMqtt(data, topic);
                    log.info("发送到mqtt\r\n名称:{}\r\n主题:{}\r\n数据：{}", "上线", topic, data);
                }
            });


            //发送设备信息到MQTT
            ThreadUtil.execAsync(new Runnable() {
                @Override
                public void run() {
                    //发送设备信息到mqtt
                    DeviceInfoUpModel deviceInfo = constructService.getDeviceInfo();
                    JSONObject jsonObject = JSONUtil.parseObj(deviceInfo, false, true);
                    String topic = "/v1/" + dictUtil.getDictValue("mqtt", "server_id") + "/devices/deviceInfo/up";
                    String data = jsonObject.toString();
                    mqttGatewayService.sendToMqtt(data, topic);
                    log.info("发送到mqtt\r\n名称:{}\r\n主题:{}\r\n数据：{}", "设备信息", topic, data);

                }
            });


        }

        if (event instanceof ContextClosedEvent) {//项目关闭
            OfflineModel offline = constructService.getOffline();
           String data = JSONUtil.parseObj(offline, false, true).toString();
            String topic = "/v1/services/offline/up";
            mqttGatewayService.sendToMqtt(data, topic);
            log.info("发送到mqtt\r\n名称:{}\r\n主题:{}\r\n数据：{}", "下线", topic, data);
        }
    }

}