package com.scdy.comprehensiveinsurance.task;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.scdy.comprehensiveinsurance.model.request.statusUpdate.StatusUpdateModel;
import com.scdy.comprehensiveinsurance.service.ConstructService;
import com.scdy.comprehensiveinsurance.service.MqttGatewayService;
import com.scdy.comprehensiveinsurance.utils.DictUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class IrTask {
//    @Autowired
//    private IrDataService irDataService;
//    @Autowired
//    private IrcameraIpService ircameraIpService;
    @Autowired
    private ConstructService constructService;
    @Autowired
    private DictUtil dictUtil;
    @Resource
    MqttGatewayService mqttGatewayService;

    private Map<String, String> md5Map = new LinkedHashMap<>();

    private String imageMd5 = null;

    /**
     * 数据更新推送数据到MQTT.
     */
//    @Scheduled(cron = "0/10 * * * * *")//每隔3秒执行一次，,必须超过判断的时间60秒，否则会重复传值
//    public void datasUp() {
//        try {
//            DatasUpModel data = constructService.getDatasUp();
//            DatasUpImageInfoModel images = data.getPacket().getImages();
//            String md5 = SecureUtil.md5(images.getFile());
//            String[] names = images.getName().split("_");
//            DateTime imageTime = DateTime.of(Long.parseLong(names[1]));
//            DateTime nowTime = DateUtil.dateSecond();
//            long betweenSecond = DateUtil.between(imageTime, nowTime, DateUnit.SECOND);
//            System.out.println("数据更新：获取到设备采集的图片时间是：" + DateUtil.format(imageTime, "yyyy-MM-dd HH:mm:ss.S"));
//            System.out.println("数据更新：当前时间:" + nowTime);
//            System.out.println("数据更新：相差秒数时间:" + betweenSecond);
//            if (betweenSecond > 60) {//设置大于60秒表示离线 TODO
//                System.out.println("===============不推送数据到MQTT===============");
//            } else {
//                if (null != imageMd5 || !md5.equals(imageMd5)) {
//                    JSONObject dataJO = JSONUtil.parseObj(data, false, true);
//                    String data1 = dataJO.toString();
//                    mqttGatewayService.sendToMqtt(data1, "/v1/"+ GlobalConstant.SERVER_ID +"/devices/datas/up");
//                    System.out.println("===============推送数据到MQTT===============" + DateUtil.now());
//                    imageMd5 = md5;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * IR摄像头是否在线
     */
    @Scheduled(cron = "0/60 * * * * *")//每隔60秒执行一次
    public void statusUpdate() {
        try {
            StatusUpdateModel statusUpdate = constructService.getDevicesStatusUpdate();
            JSONObject dataJO = JSONUtil.parseObj(statusUpdate, false, true);
            String data = dataJO.toString();
            String topic = "/v1/" + dictUtil.getDictValue("mqtt", "server_id") + "/devices/status/update";
            mqttGatewayService.sendToMqtt(data, topic);
            log.info("发送到mqtt\r\n名称:{}\r\n主题:{}\r\n数据：{}", "设备状态更新", topic, data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * IR图片获取并分析
     */
//    @Scheduled(cron = "0/10 * * * * *")//每隔30秒执行一次
//    public void startGetIRCamera() {
//        ThreadUtil.execAsync(new Runnable() {
//            @Override
//            public void run() {
////                        StartGetIRCamera sIrCamera = new StartGetIRCamera();
////                        sIrCamera.start();
//                StartGetPNS64Camera sIrCamera = new StartGetPNS64Camera();
//                sIrCamera.run();
//            }
//        });
//    }


}