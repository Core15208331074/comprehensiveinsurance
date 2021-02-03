package com.scdy.comprehensiveinsurance.service.impl;

import cn.hutool.json.JSONUtil;
import com.scdy.comprehensiveinsurance.model.PointModel;
import com.scdy.comprehensiveinsurance.model.SensorModel;
import com.scdy.comprehensiveinsurance.service.InfluxdbService;
import com.scdy.comprehensiveinsurance.utils.SpringUtil;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Transactional
@Service
public class InfluxdbServiceImpl implements InfluxdbService {
    @Override
    public void saveEthToInfluxdb(SensorModel sensorModel) {
        List<PointModel> pointModelList = sensorModel.getPointModelList();


        //如果返回指令是空则不存入时序数据库
        if (pointModelList.size() < 1) {
            return;
        }

        PointModel pointModel1 = null;
        for (PointModel pointModel : pointModelList) {
            pointModel1 = pointModel;
            String responseInstruction = pointModel.getResponseInstruction();
            if (StringUtils.isEmpty(responseInstruction)) {
                return;
            }
        }

        //存储数据到influxDB
        Point.Builder builder = Point.measurement("sensorModel");
        if (!StringUtils.isEmpty(sensorModel.getIsControl())) {
            builder.tag("isControl", sensorModel.getIsControl());
        } else {
            builder.tag("isControl", "0");//0表示查询，1表示反控
        }

        if (!StringUtils.isEmpty(sensorModel.getSensorNameId())) {
            builder.tag("sensorNameId", sensorModel.getSensorNameId());
        }
        builder.tag("pointName",pointModel1.getPointName());
        builder.tag("deviceAddress", sensorModel.getDeviceAddress());
        builder.tag("myTag", sensorModel.getTag());
        builder.tag("sensorName", sensorModel.getSensorName());
        builder.tag("type", sensorModel.getType());//GlobalConstants.TCP_CLIENT

        builder.addField("json", JSONUtil.parseObj(sensorModel).toString());
        Point point = builder.build();
        InfluxDB influxDB = SpringUtil.getBean(InfluxDB.class);
        Environment environment = SpringUtil.getBean(Environment.class);
        influxDB.setDatabase(environment.getProperty("spring.influx.database"));
        if (!influxDB.databaseExists(environment.getProperty("spring.influx.database"))) {//数据库不存在
            //创建数据库
            influxDB.createDatabase(environment.getProperty("spring.influx.database"));
            //创建存储策略，存储30天自动删除  rpName,  database,  duration,  replicationFactor,  isDefault
            influxDB.createRetentionPolicy(environment.getProperty("spring.influx.rpName"), environment.getProperty("spring.influx.database"), environment.getProperty("spring.influx.duration"), Integer.parseInt(environment.getProperty("spring.influx.replicationFactor")), true);
        }
        influxDB.setRetentionPolicy(environment.getProperty("spring.influx.rpName"));
        influxDB.write(point);


    }
}
