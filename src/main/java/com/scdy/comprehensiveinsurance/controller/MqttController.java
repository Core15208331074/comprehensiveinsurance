package com.scdy.comprehensiveinsurance.controller;

import com.scdy.comprehensiveinsurance.service.MqttGatewayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Arrays;

@Controller
@RequestMapping("mqtt")
public class MqttController {
    @Value("${mqtt.filter}")
    private String[] filter;
    @Resource
    MqttGatewayService mqttGatewayService;

    /**
     * 从mqtt接收消息
     */
//    @GetMapping("getMsgFromMqtt")
//    @ResponseBody
//    public void getMsgFromMqtt() {
//        System.out.println(filter);
//    }

    /**
     * 发送消息到MQTT
     */
    @PostMapping("/sendMsgToMqtt")
    @ResponseBody
    public void sendMsgToMqtt() {
        System.out.println("123");
        mqttGatewayService.sendToMqtt("测试", "comprehensiveinsurance");
    }

    /**
     * 判断数据是否为当前平台数据
     * MQTT传递所有平台数据
     *
     * @param origin
     * @return
     */
    private boolean isCurrentPlatformData(String origin) {
        return !StringUtils.isEmpty(origin) && Arrays.asList(filter).contains(origin);
    }
}
