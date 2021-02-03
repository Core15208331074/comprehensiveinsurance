package com.scdy.comprehensiveinsurance.service;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;

/**
 * MqttGateway消息推送接口类
 */
@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttGatewayService {
    void sendToMqtt(String data, @Header(MqttHeaders.TOPIC) String topic);
}