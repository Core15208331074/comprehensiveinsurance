package com.scdy.comprehensiveinsurance.service.impl;


import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scdy.comprehensiveinsurance.constant.GlobalConstants;
import com.scdy.comprehensiveinsurance.dao.EthMapper;
import com.scdy.comprehensiveinsurance.drive.tcp.client.TcpClient;
import com.scdy.comprehensiveinsurance.drive.tcp.client.TcpClientOperation;
import com.scdy.comprehensiveinsurance.drive.tcp.server.TcpServer;
import com.scdy.comprehensiveinsurance.drive.tcp.server.TcpServerOperation;
import com.scdy.comprehensiveinsurance.entity.DictEntity;
import com.scdy.comprehensiveinsurance.entity.EthEntity;
import com.scdy.comprehensiveinsurance.entity.SensorEntity;
import com.scdy.comprehensiveinsurance.model.*;
import com.scdy.comprehensiveinsurance.service.*;
import com.scdy.comprehensiveinsurance.utils.DictUtil;
import com.scdy.comprehensiveinsurance.utils.InfluxDBUtil;
import com.scdy.comprehensiveinsurance.utils.udpUtil.UDPUtil;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zl
 * @since 2020-11-24
 */
@Slf4j
@Service
public class EthServiceImpl extends ServiceImpl<EthMapper, EthEntity> implements EthService {
    @Autowired
    private DictService dictService;
    @Autowired
    private SensorService sensorService;
    @Autowired
    private SensorDataService sensorDataService;
    @Autowired
    private InfluxDB influxDB;
    @Autowired
    private DictUtil dictUtil;
    @Autowired
    private ParamService paramService;
    @Resource
    MqttGatewayService mqttGatewayService;
    @Value("${spring.influx.database:comprehensiveinsurance}")
    private String database;


    @Override
    public PageModel<SensorModel> getSensorDataPage(ComRequestModel comRequestModel) throws Exception {
        String id = comRequestModel.getId();
        Date startTime = comRequestModel.getStartTime();
        Date endTime = comRequestModel.getEndTime();
        int pageIndex = comRequestModel.getPageIndex();
        int pageSize = comRequestModel.getPageSize();

        PageModel<SensorModel> sensorModelPageModel = new PageModel<>();
        sensorModelPageModel.setCurrentPage(pageIndex);
        sensorModelPageModel.setPageSize(pageSize);


        EthEntity ethEntity = this.getById(id);


        //开始时间
        String startTimeStr = "";
        if (!StringUtils.isEmpty(startTime) && startTime.getTime() > 0) {
            startTimeStr = " and time >=" + startTime.getTime() + "ms ";//毫秒
        }

        //结束时间
        String endTimeStr = "";
        if (!StringUtils.isEmpty(endTime) && endTime.getTime() > 0) {
            endTimeStr = " and time <=" + endTime.getTime() + "ms ";//毫秒
        }


        if ("7".equals(ethEntity.getSensorModel())) {//蓄电池

            ArrayList<List<Object>> lists = new ArrayList<>();
            List<Object> json1 = getObjectList(pageIndex, pageSize, ethEntity, startTimeStr, endTimeStr, GlobalConstants.BATTERY_LITONG_TELEMETERING_0_39);
            if (!StringUtils.isEmpty(json1)) {
                lists.add(json1);
            }
            List<Object> json2 = getObjectList(pageIndex, pageSize, ethEntity, startTimeStr, endTimeStr, GlobalConstants.BATTERY_LITONG_TELEMETERING_40_79);
            if (!StringUtils.isEmpty(json2)) {
                lists.add(json2);
            }
            List<Object> json3 = getObjectList(pageIndex, pageSize, ethEntity, startTimeStr, endTimeStr, GlobalConstants.BATTERY_LITONG_TELEMETERING_1000_1039);
            if (!StringUtils.isEmpty(json3)) {
                lists.add(json3);
            }
            List<Object> json4 = getObjectList(pageIndex, pageSize, ethEntity, startTimeStr, endTimeStr, GlobalConstants.BATTERY_LITONG_TELEMETERING_1040_1079);
            if (!StringUtils.isEmpty(json4)) {
                lists.add(json4);
            }
            List<Object> json5 = getObjectList(pageIndex, pageSize, ethEntity, startTimeStr, endTimeStr, GlobalConstants.BATTERY_LITONG_TELEMETERING_1080_1119);
            if (!StringUtils.isEmpty(json5)) {
                lists.add(json5);
            }
            List<Object> json6 = getObjectList(pageIndex, pageSize, ethEntity, startTimeStr, endTimeStr, GlobalConstants.BATTERY_LITONG_TELEMETERING_1120_1159);
            if (!StringUtils.isEmpty(json6)) {
                lists.add(json6);
            }
            List<Object> json7 = getObjectList(pageIndex, pageSize, ethEntity, startTimeStr, endTimeStr, GlobalConstants.BATTERY_LITONG_TELEMETERING_1160_1207);
            if (!StringUtils.isEmpty(json7)) {
                lists.add(json7);
            }
            List<Object> json8 = getObjectList(pageIndex, pageSize, ethEntity, startTimeStr, endTimeStr, GlobalConstants.BATTERY_LITONG_TELEMETERING_144);
            if (!StringUtils.isEmpty(json8)) {
                lists.add(json8);
            }

            List<Object> objects = null;
            if (lists.size() > 0) {
                objects = lists.get(0);
            }
            ArrayList<SensorModel> jsonList = new ArrayList<>();
            for (int i = 0; i < objects.size(); i++) {
                ArrayList<SensorModel> sensorModels = new ArrayList<>();
                for (List<Object> list : lists) {
                    Object o = list.get(i);
                    if (!StringUtils.isEmpty(o)) {
                        SensorModel sensorModel = JSONUtil.toBean(o.toString(), SensorModel.class);
                        sensorModels.add(sensorModel);
                    }
                }

                SensorModel sensorModel = null;
                if (sensorModels.size() > 0) {
                    sensorModel = sensorModels.get(0);
                }

                for (int j = 1; j < sensorModels.size(); j++) {
                    SensorModel sensorModel9 = sensorModels.get(j);
                    if (!StringUtils.isEmpty(sensorModel9)) {
                        List<PointModel> pointModelList = sensorModel9.getPointModelList();
                        for (PointModel pointModel : pointModelList) {
                            List<PointModel> pointModelList1 = sensorModel.getPointModelList();
                            pointModelList1.add(pointModel);
                        }
                    }
                }
                jsonList.add(sensorModel);

            }


//            for (Object o : json2) {
//                SensorModel sensorModel = JSONUtil.toBean(o.toString(), SensorModel.class);
//                jsonList.add(sensorModel);
//            }
            sensorModelPageModel.setDatalist(jsonList);


            String mysql2 = "select COUNT(json)  from sensorModel  where  isControl='0' and " +
                    "type='" + ethEntity.getEthName() + "'" +
                    startTimeStr + endTimeStr +
                    "and myTag='" + ethEntity.getTag() + "'  and deviceAddress='" + ethEntity.getDeviceAddress() + "'   and  sensorNameId ='" + ethEntity.getSensorModel() + "'";

            QueryResult query = influxDB.query(new Query(mysql2, database));

            List<Object> countList = InfluxDBUtil.getListByField(query, "count");
            for (Object count : countList) {
                Double aDouble = Double.parseDouble(count.toString());
                Double v = aDouble / 5;
                Double ceil = Math.ceil(v);
                sensorModelPageModel.setTotalPage(ceil.longValue());
            }
            return sensorModelPageModel;
        } else {


            String mysql1 = "select * from sensorModel  where  isControl='0' and " +
                    "type='" + ethEntity.getEthName() + "'" +
                    startTimeStr + endTimeStr +

                    " and myTag='" + ethEntity.getTag() + "' and deviceAddress='" + ethEntity.getDeviceAddress() + "'  and sensorNameId ='" + ethEntity.getSensorModel() + "' ORDER BY time DESC limit " + pageSize + " offset " + (pageIndex - 1) * pageSize;
            QueryResult rs = influxDB.query(new Query(mysql1, database));


            List<Object> json2 = InfluxDBUtil.getListByField(rs, "json");
            if (StringUtils.isEmpty(json2)) {
                return null;
            }
            ArrayList<SensorModel> jsonList = new ArrayList<>();
            for (Object o : json2) {
                SensorModel sensorModel = JSONUtil.toBean(o.toString(), SensorModel.class);
                jsonList.add(sensorModel);
            }
            sensorModelPageModel.setDatalist(jsonList);


            String mysql2 = "select COUNT(json)  from sensorModel  where  isControl='0' and " +
                    "type='" + ethEntity.getEthName() + "'" +
                    startTimeStr + endTimeStr +
                    "and myTag='" + ethEntity.getTag() + "'  and deviceAddress='" + ethEntity.getDeviceAddress() + "'  and  sensorNameId ='" + ethEntity.getSensorModel() + "'";

            QueryResult query = influxDB.query(new Query(mysql2, database));

            List<Object> countList = InfluxDBUtil.getListByField(query, "count");
            for (Object count : countList) {
                Double aDouble = Double.parseDouble(count.toString());
                sensorModelPageModel.setTotalPage(aDouble.longValue());
            }
            return sensorModelPageModel;

        }


    }

    private List<Object> getObjectList(int pageIndex, int pageSize, EthEntity ethEntity, String startTimeStr, String endTimeStr, String pointName) {
        String mysql1 = "select * from sensorModel  where  isControl='0' and " +
                "type='" + ethEntity.getEthName() + "'" +
                startTimeStr + endTimeStr +

                " and myTag='" + ethEntity.getTag() + "' and deviceAddress='" + ethEntity.getDeviceAddress() + "' and pointName='" + pointName + "'  and sensorNameId ='" + ethEntity.getSensorModel() + "' ORDER BY time DESC limit " + pageSize + " offset " + (pageIndex - 1) * pageSize;
        QueryResult rs = influxDB.query(new Query(mysql1, database));


        List<Object> json2 = InfluxDBUtil.getListByField(rs, "json");
        if (StringUtils.isEmpty(json2)) {
            return null;
        }
        return json2;
    }

    @Override
    public PageModel<SensorModel> getSensorDataPage(String id, int pageIndex, int pageSize) {
        PageModel<SensorModel> sensorModelPageModel = new PageModel<>();
        sensorModelPageModel.setCurrentPage(pageIndex);
        sensorModelPageModel.setPageSize(pageSize);

        QueryWrapper<EthEntity> ethEntityQueryWrapper = new QueryWrapper<>();
        ethEntityQueryWrapper.eq("is_deleted", 0);
        ethEntityQueryWrapper.eq("id", id);
        List<EthEntity> ethEntityList = this.list(ethEntityQueryWrapper);
        EthEntity ethEntity = ethEntityList.get(0);
        QueryResult rs = influxDB.query(new Query("select * from sensorModel  where type='" + GlobalConstants.TCP_CLIENT + "' and sensorNameId ='" + ethEntity.getSensorModel() + "' ORDER BY time DESC limit " + pageSize + " offset " + (pageIndex - 1) * pageSize, database));
        List<Object> json2 = InfluxDBUtil.getListByField(rs, "json");
        if (StringUtils.isEmpty(json2)) {
            return null;
        }
        ArrayList<SensorModel> jsonList = new ArrayList<>();
        for (Object o : json2) {
            SensorModel sensorModel = JSONUtil.toBean(o.toString(), SensorModel.class);
            jsonList.add(sensorModel);
        }
        sensorModelPageModel.setDatalist(jsonList);

        QueryResult query = influxDB.query(new Query("select COUNT(json)  from sensorModel  where type='" + GlobalConstants.TCP_CLIENT + "' and  sensorNameId ='" + ethEntity.getSensorModel() + "'", database));
        List<Object> countList = InfluxDBUtil.getListByField(query, "count");
        for (Object count : countList) {
            Double aDouble = Double.parseDouble(count.toString());
            sensorModelPageModel.setTotalPage(aDouble.longValue());
        }
        return sensorModelPageModel;
    }

    @Override
    public SensorModel getRealtimeData(String id) {
        EthEntity ethEntity = this.getById(id);

        if ("7".equals(ethEntity.getSensorModel())) {//利通蓄电池
            ArrayList<SensorModel> sensorModelList = new ArrayList<>();
            SensorModel sensorModel1 = getSensorModel(ethEntity, GlobalConstants.BATTERY_LITONG_TELEMETERING_0_39);
            if (!StringUtils.isEmpty(sensorModel1)) {
                sensorModelList.add(sensorModel1);
            }
            SensorModel sensorModel2 = getSensorModel(ethEntity, GlobalConstants.BATTERY_LITONG_TELEMETERING_40_79);
            if (!StringUtils.isEmpty(sensorModel2)) {
                sensorModelList.add(sensorModel2);
            }
            SensorModel sensorModel3 = getSensorModel(ethEntity, GlobalConstants.BATTERY_LITONG_TELEMETERING_1000_1039);
            if (!StringUtils.isEmpty(sensorModel3)) {
                sensorModelList.add(sensorModel3);
            }
            SensorModel sensorModel4 = getSensorModel(ethEntity, GlobalConstants.BATTERY_LITONG_TELEMETERING_1040_1079);
            if (!StringUtils.isEmpty(sensorModel4)) {
                sensorModelList.add(sensorModel4);
            }
            SensorModel sensorModel5 = getSensorModel(ethEntity, GlobalConstants.BATTERY_LITONG_TELEMETERING_1080_1119);
            if (!StringUtils.isEmpty(sensorModel5)) {
                sensorModelList.add(sensorModel5);
            }
            SensorModel sensorModel6 = getSensorModel(ethEntity, GlobalConstants.BATTERY_LITONG_TELEMETERING_1120_1159);
            if (!StringUtils.isEmpty(sensorModel6)) {
                sensorModelList.add(sensorModel6);
            }
            SensorModel sensorModel7 = getSensorModel(ethEntity, GlobalConstants.BATTERY_LITONG_TELEMETERING_1160_1207);
            if (!StringUtils.isEmpty(sensorModel7)) {
                sensorModelList.add(sensorModel7);
            }
            SensorModel sensorModel8 = getSensorModel(ethEntity, GlobalConstants.BATTERY_LITONG_TELEMETERING_144);
            if (!StringUtils.isEmpty(sensorModel8)) {
                sensorModelList.add(sensorModel8);
            }

            SensorModel sensorModel = null;
            if (sensorModelList.size() > 0) {
                sensorModel = sensorModelList.get(0);
            }

            for (int i = 1; i < sensorModelList.size(); i++) {
                SensorModel sensorModel9 = sensorModelList.get(i);
                if (!StringUtils.isEmpty(sensorModel9)) {
                    List<PointModel> pointModelList = sensorModel9.getPointModelList();
                    for (PointModel pointModel : pointModelList) {
                        List<PointModel> pointModelList1 = sensorModel.getPointModelList();
                        pointModelList1.add(pointModel);
                    }
                }
            }

            return sensorModel;
        } else if ("14".equals(ethEntity.getSensorModel())) {//峨山蓄电池
            ArrayList<SensorModel> sensorModelList = new ArrayList<>();
            SensorModel sensorModel1_100 = getSensorModel(ethEntity, GlobalConstants.BATTERY_MOUNT_E_SINGLE_VOLTAGE_1_100);
            if (!StringUtils.isEmpty(sensorModel1_100)) {
                sensorModelList.add(sensorModel1_100);
            }
            SensorModel sensorModel101_200 = getSensorModel(ethEntity, GlobalConstants.BATTERY_MOUNT_E_SINGLE_VOLTAGE_101_200);
            if (!StringUtils.isEmpty(sensorModel101_200)) {
                sensorModelList.add(sensorModel101_200);
            }
            SensorModel sensorModel201_300 = getSensorModel(ethEntity, GlobalConstants.BATTERY_MOUNT_E_SINGLE_VOLTAGE_201_300);
            if (!StringUtils.isEmpty(sensorModel201_300)) {
                sensorModelList.add(sensorModel201_300);
            }
            SensorModel monomerResistanceSensorModel1_100 = getSensorModel(ethEntity, GlobalConstants.BATTERY_MOUNT_E_MONOMER_RESISTANCE_1_100);
            if (!StringUtils.isEmpty(monomerResistanceSensorModel1_100)) {
                sensorModelList.add(monomerResistanceSensorModel1_100);
            }
            SensorModel monomerResistanceSensorModel101_200 = getSensorModel(ethEntity, GlobalConstants.BATTERY_MOUNT_E_MONOMER_RESISTANCE_101_200);
            if (!StringUtils.isEmpty(monomerResistanceSensorModel101_200)) {
                sensorModelList.add(monomerResistanceSensorModel101_200);
            }
            SensorModel monomerResistanceSensorModel201_300 = getSensorModel(ethEntity, GlobalConstants.BATTERY_MOUNT_E_MONOMER_RESISTANCE_201_300);
            if (!StringUtils.isEmpty(monomerResistanceSensorModel201_300)) {
                sensorModelList.add(monomerResistanceSensorModel201_300);
            }
            SensorModel batteryTemperatureSensorModel1_100 = getSensorModel(ethEntity, GlobalConstants.BATTERY_MOUNT_E_BATTERY_TEMPERATURE_1_100);
            if (!StringUtils.isEmpty(batteryTemperatureSensorModel1_100)) {
                sensorModelList.add(batteryTemperatureSensorModel1_100);
            }
            SensorModel batteryTemperatureSensorModel101_200 = getSensorModel(ethEntity, GlobalConstants.BATTERY_MOUNT_E_BATTERY_TEMPERATURE_101_200);
            if (!StringUtils.isEmpty(batteryTemperatureSensorModel101_200)) {
                sensorModelList.add(batteryTemperatureSensorModel101_200);
            }
            SensorModel batteryTemperatureSensorModel201_300 = getSensorModel(ethEntity, GlobalConstants.BATTERY_MOUNT_E_BATTERY_TEMPERATURE_201_300);
            if (!StringUtils.isEmpty(batteryTemperatureSensorModel201_300)) {
                sensorModelList.add(batteryTemperatureSensorModel201_300);
            }
            SensorModel totalVoltageSensorModel = getSensorModel(ethEntity, GlobalConstants.BATTERY_MOUNT_E_TOTAL_VOLTAGE);
            if (!StringUtils.isEmpty(totalVoltageSensorModel)) {
                sensorModelList.add(totalVoltageSensorModel);
            }
            SensorModel sensorModel = null;
            if (sensorModelList.size() > 0) {
                sensorModel = sensorModelList.get(0);
            }

            for (int i = 1; i < sensorModelList.size(); i++) {
                SensorModel sensorModel9 = sensorModelList.get(i);
                if (!StringUtils.isEmpty(sensorModel9)) {
                    List<PointModel> pointModelList = sensorModel9.getPointModelList();
                    for (PointModel pointModel : pointModelList) {
                        List<PointModel> pointModelList1 = sensorModel.getPointModelList();
                        pointModelList1.add(pointModel);
                    }
                }
            }
            return sensorModel;
        } else {
            QueryResult rs = influxDB.query(new Query("select * from sensorModel  where isControl='0' and type='" + ethEntity.getEthName() + "' and deviceAddress='" + ethEntity.getDeviceAddress() + "' and myTag='" + ethEntity.getTag() + "' and sensorNameId ='" + ethEntity.getSensorModel() + "' ORDER BY time DESC limit 1", database));
            List<QueryResult.Result> results = rs.getResults();
            QueryResult.Result result1 = results.get(0);
            List<QueryResult.Series> series2 = result1.getSeries();
            if (StringUtils.isEmpty(series2)) {
                return null;
            }
            QueryResult.Series series3 = series2.get(0);
            List<String> columns1 = series3.getColumns();
            int json1 = columns1.indexOf("json");
            ArrayList<String> jsonList = new ArrayList<>();
            List<List<Object>> values1 = series3.getValues();
            List<Object> objects1 = values1.get(0);
            Object json = objects1.get(json1);
            SensorModel sensorModel = JSONUtil.toBean(json.toString(), SensorModel.class);
            return sensorModel;
        }


    }

    private SensorModel getSensorModel(EthEntity ethEntity, String pointName) {
        QueryResult rs = influxDB.query(new Query("select * from sensorModel  where isControl='0' and type='" + ethEntity.getEthName() + "'  and pointName='" + pointName + "' and deviceAddress='" + ethEntity.getDeviceAddress() + "' and myTag='" + ethEntity.getTag() + "' and sensorNameId ='" + ethEntity.getSensorModel() + "' ORDER BY time DESC limit 1", database));
        List<QueryResult.Result> results = rs.getResults();
        QueryResult.Result result1 = results.get(0);
        List<QueryResult.Series> series2 = result1.getSeries();
        if (StringUtils.isEmpty(series2)) {
            return null;
        }
        QueryResult.Series series3 = series2.get(0);
        List<String> columns1 = series3.getColumns();
        int json1 = columns1.indexOf("json");
        List<List<Object>> values1 = series3.getValues();
        List<Object> objects1 = values1.get(0);
        Object json = objects1.get(json1);
        SensorModel sensorModel = JSONUtil.toBean(json.toString(), SensorModel.class);
        return sensorModel;
    }

    @Override
    public boolean isStartById(String ethId, boolean isStart) throws Exception {
        if (isStart) {
            //启动
            return startById(ethId);
        } else {
            //关闭
            return stopById(ethId);
        }
    }

    /**
     * 关闭eth
     *
     * @param ethId
     * @return
     */
    private synchronized boolean stopById(String ethId) throws Exception {
        EthEntity ethEntity = this.getById(ethId);
        if (!StringUtils.isEmpty(ethEntity)) {
            ethEntity.setIsStart(false);


            LinkedHashMap<String, LinkedHashMap<String, EthStatusModel>> ethStatusMap = GlobalConstants.getEthStatusMap();
            String key = ethEntity.getEthName() + "_" + ethEntity.getTargetIp() + "_" + ethEntity.getTargetPort() + "_" + ethEntity.getLocalPort();
            LinkedHashMap<String, EthStatusModel> ethStatusModelLinkedHashMap = ethStatusMap.get(key);
            if (StringUtils.isEmpty(ethStatusModelLinkedHashMap)) {
                //表示这个没启动
                return false;

            } else {
                //表示已经启动
                for (Map.Entry<String, EthStatusModel> ethStatusModelEntry : ethStatusModelLinkedHashMap.entrySet()) {
                    String key1 = ethStatusModelEntry.getKey();
                    EthStatusModel ethStatusModel = ethStatusModelEntry.getValue();

                    EthEntity ethEntity1 = ethStatusModel.getEthEntity();
                    //ethEntity.getSensorModel() + "_" + ethEntity.getDeviceAddress()+"_"+ethEntity.getTag()
                    if ((ethEntity.getTargetIp() + "_" + ethEntity.getTargetPort() + "_" + ethEntity.getLocalPort()).equals(key1)) {
                        ethEntity1.setIsStart(false);
//                        ethStatusModel.setStart(false);


                        String ethName = ethEntity1.getEthName();
                        switch (ethName) {
                            case GlobalConstants.TCP_CLIENT:
                                //关闭
                                Map<String, TcpClient> tcpClientMap = GlobalConstants.getTcpClientMap();
                                String key2 = ethEntity.getTargetIp() + ":" + ethEntity.getTargetPort();
                                TcpClient tcpClient = tcpClientMap.get(key2);
                                if (!StringUtils.isEmpty(tcpClient)) {
                                    tcpClient.getGroup().shutdownGracefully().sync();
                                }
                                break;
                            case GlobalConstants.TCP_SERVER:
                                //关闭
                                Map<String, TcpServer> tcpServerMap = GlobalConstants.getTcpServerMap();
                                String key3 = ethEntity.getLocalPort();
                                TcpServer tcpServer = tcpServerMap.get(key3);
                                if (!StringUtils.isEmpty(tcpServer)) {
                                    tcpServer.getBoss().shutdownGracefully().sync();
                                    tcpServer.getWorker().shutdownGracefully().sync();
                                }
                                break;
                            case GlobalConstants.UDP_CLIENT:
                                //TODO UDP client开发
                                break;
                            case GlobalConstants.UDP_SERVER:
                                //TODO UDP server开发
                                break;
                        }


                    }


                }
////                -------
//                Iterator<String> iterator = ethStatusModelLinkedHashMap.keySet().iterator();
//                while (iterator.hasNext()) {
//                    String key1 = iterator.next();
//                    if (ethEntity.getDeviceAddress().equals(key1)) {
//                        iterator.remove();
//                    }
//                }
//
//                if (ethStatusModelLinkedHashMap.size() < 1) {
//                    Iterator<String> iterator1 = ethStatusMap.keySet().iterator();
//                    while (iterator1.hasNext()) {
//                        String key1 = iterator1.next();
//                        if (key.equals(key1)) {
//                            iterator1.remove();
//                        }
//                    }
//
//                }
            }
            return false;
        }
        return false;
    }

    /**
     * 启动eth
     *
     * @param ethId
     * @return
     */
    private synchronized boolean startById(String ethId) {
        EthEntity ethEntity = this.getById(ethId);
        if (!StringUtils.isEmpty(ethEntity)) {
            ethEntity.setIsStart(true);//设置为启动状态
            LinkedHashMap<String, LinkedHashMap<String, EthStatusModel>> ethStatusMap = GlobalConstants.getEthStatusMap();
            String key = ethEntity.getEthName() + "_" + ethEntity.getTargetIp() + "_" + ethEntity.getTargetPort() + "_" + ethEntity.getLocalPort();
            LinkedHashMap<String, EthStatusModel> ethStatusModelLinkedHashMap = ethStatusMap.get(key);


            EthStatusModel ethStatusModel = new EthStatusModel();
            //构建请求指令
            SensorModel sensorModel = sensorDataService.getSensorSendData(ethEntity);
            ethStatusModel.setSensorModel(sensorModel);
            ethStatusModel.setEthEntity(ethEntity);
//            ethStatusModel.setStart(true);

            if (StringUtils.isEmpty(ethStatusModelLinkedHashMap)) {
                //没找到
                LinkedHashMap<String, EthStatusModel> ethStatusModelLinkedHashMap1 = new LinkedHashMap<>();
                ethStatusModelLinkedHashMap1.put(ethEntity.getTargetIp() + "_" + ethEntity.getTargetPort() + "_" + ethEntity.getLocalPort(), ethStatusModel);
                ethStatusMap.put(key, ethStatusModelLinkedHashMap1);

                try {
                    CronUtil.stop();//停止任务调度
                } catch (Exception e) {
                }

                ThreadUtil.execAsync(new Runnable() {
                    @Override
                    public void run() {
//                        --------------------------------------------------------------------


//                        --------------------------------------------------------------------
                        String schedule = CronUtil.schedule("0/20 * * * * ? ", new Task() {
                            @Override
                            public void execute() {
                                try {
                                    LinkedHashMap<String, LinkedHashMap<String, EthStatusModel>> ethStatusMap1 = GlobalConstants.getEthStatusMap();
                                    for (LinkedHashMap<String, EthStatusModel> ethStatusModelLinkedHashMap2 : ethStatusMap1.values()) {
                                        //                                    LinkedHashMap<String, EthStatusModel> ethStatusModelLinkedHashMap2 = ethStatusMap1.get(key);
                                        for (Map.Entry<String, EthStatusModel> entry : ethStatusModelLinkedHashMap2.entrySet()) {
                                            String address = entry.getKey();//地址
                                            EthStatusModel entryValue = entry.getValue();//串口状态
                                            if (entryValue.getEthEntity().getIsStart()) {//entryValue.isStart()
                                                SensorModel sensorModel1 = entryValue.getSensorModel();
                                                EthEntity entity = entryValue.getEthEntity();
                                                sensorModel1.setIsControl(null);

                                                String ethName = entity.getEthName();
                                                switch (ethName) {
                                                    case GlobalConstants.TCP_CLIENT:
                                                        TcpClientOperation tcpClientOperation = new TcpClientOperation(entity, sensorModel1);
                                                        tcpClientOperation.startRun();
                                                        break;
                                                    case GlobalConstants.TCP_SERVER:
                                                        TcpServerOperation tcpServerOperation = new TcpServerOperation(entity, sensorModel1);
                                                        tcpServerOperation.startRun();
                                                        break;
                                                    case GlobalConstants.UDP_CLIENT:
                                                        //TODO UDP client开发
                                                        byte[] bytes = {0x00, 0x02, 0x00, 0x00, 0x00, 0x00};
                                                        UDPUtil.sendData(bytes);
                                                        String hexadecimalData = UDPUtil.receiveHexadecimalData();
                                                        JSONObject data = UDPUtil.resolvingData(hexadecimalData);
                                                        mqttGatewayService.sendToMqtt(data.toJSONString(), "/v1/" + dictUtil.getDictValue("mqtt", "server_id") + "/devices/datas/up");
                                                        break;
                                                    case GlobalConstants.UDP_SERVER:
                                                        //TODO UDP server开发
                                                        break;
                                                }

                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        // 支持秒级别定时任务
                        CronUtil.setMatchSecond(true);
                        CronUtil.start();

//                        Scheduler scheduler = CronUtil.getScheduler();
//                        scheduler.addListener(new TaskListener() {
//                            @Override
//                            public void onStart(TaskExecutor taskExecutor) {
//                            }
//
//                            @Override
//                            public void onSucceeded(TaskExecutor taskExecutor) {
//                            }
//
//                            @Override
//                            public void onFailed(TaskExecutor taskExecutor, Throwable throwable) {
//                                CronUtil.getScheduler().clear();
//                                System.out.println("轮询线程结束----------------------------------------------------------");
//                            }
//                        });
                    }
                });

            } else {
                //找到
                ethStatusModelLinkedHashMap.put(ethEntity.getTargetIp() + "_" + ethEntity.getTargetPort() + "_" + ethEntity.getLocalPort(), ethStatusModel);
            }
            return true;
        }
        return false;
    }


    @Override
    public PageModel<EthEntity> getEthList(int pageIndex, int pageSize) {


        QueryWrapper<EthEntity> ethEntityQueryWrapper = new QueryWrapper<>();
        ethEntityQueryWrapper.eq("is_deleted", "0");

        Page<EthEntity> ethEntityPage = new Page<>(pageIndex, pageSize, true);
        Page<EthEntity> page = this.page(ethEntityPage, ethEntityQueryWrapper);
        List<EthEntity> records = page.getRecords();
        for (EthEntity ethEntity : records) {
            String sensorModelId = ethEntity.getSensorModel();

            QueryWrapper<SensorEntity> sensorEntityQueryWrapper = new QueryWrapper<>();
            sensorEntityQueryWrapper.eq("is_deleted", "0");
            sensorEntityQueryWrapper.eq("sensor_name_code", sensorModelId);
            sensorEntityQueryWrapper.eq("type", "eth");
            List<SensorEntity> list1 = sensorService.list(sensorEntityQueryWrapper);
            String sensorModelCode = ethEntity.getSensorModel();
            for (SensorEntity sensorEntity : list1) {
                String sensorName = sensorEntity.getSensorName();
                ethEntity.setSensorModel(sensorName);
            }
            String key = ethEntity.getEthName() + "_" + ethEntity.getTargetIp() + "_" + ethEntity.getTargetPort() + "_" + ethEntity.getLocalPort();
            LinkedHashMap<String, LinkedHashMap<String, EthStatusModel>> ethStatusMap = GlobalConstants.getEthStatusMap();
            LinkedHashMap<String, EthStatusModel> stringEthStatusModelLinkedHashMap = ethStatusMap.get(key);
            if (!StringUtils.isEmpty(stringEthStatusModelLinkedHashMap)) {
                EthStatusModel ethStatusModel = stringEthStatusModelLinkedHashMap.get(ethEntity.getTargetIp() + "_" + ethEntity.getTargetPort() + "_" + ethEntity.getLocalPort());//sensorModelCode + "_" + ethEntity.getDeviceAddress()+"_"+ethEntity.getTag()
                if (StringUtils.isEmpty(ethStatusModel)) {
                    ethEntity.setIsStart(false);
                } else {
                    ethEntity.setIsStart(ethStatusModel.getEthEntity().getIsStart());//ethStatusModel.isStart()
                }
            } else {
                ethEntity.setIsStart(false);
            }

        }

        PageModel<EthEntity> ethEntityPageModel = new PageModel<>();
        ethEntityPageModel.setCurrentPage(Math.toIntExact(page.getCurrent()));
        ethEntityPageModel.setPageSize(Math.toIntExact(page.getSize()));
        ethEntityPageModel.setDatalist(records);
        ethEntityPageModel.setTotalPage(page.getTotal());

        return ethEntityPageModel;
    }

    @Override
    public Map<String, Object> getConfig() {
        HashMap<String, Object> map = new HashMap<>();

        //通讯模式
        QueryWrapper<DictEntity> dictEntityQueryWrapper = new QueryWrapper<>();
        dictEntityQueryWrapper.eq("is_deleted", 0);
        dictEntityQueryWrapper.eq("name", "eth");
        List<DictEntity> list = dictService.list(dictEntityQueryWrapper);
        map.put("ethList", list);

        //传感器型号
        QueryWrapper<SensorEntity> sensorEntityQueryWrapper = new QueryWrapper<>();
        sensorEntityQueryWrapper.eq("is_deleted", 0);
        sensorEntityQueryWrapper.eq("type", "eth");
        List<SensorEntity> sensorList = sensorService.list(sensorEntityQueryWrapper);
        map.put("sensorList", sensorList);

        return map;
    }

    @Override
    public boolean saveEth(EthEntity ethEntity) {
        this.save(ethEntity);
        return true;
//        QueryWrapper<EthEntity> ethEntityQueryWrapper = new QueryWrapper<>();
//        ethEntityQueryWrapper.eq("eth_name", ethEntity.getEthName());
//        ethEntityQueryWrapper.eq("target_ip", ethEntity.getTargetIp());
//        ethEntityQueryWrapper.eq("target_port", ethEntity.getTargetPort());
//        ethEntityQueryWrapper.eq("local_port", ethEntity.getLocalPort());
//        ethEntityQueryWrapper.eq("sensor_model", ethEntity.getSensorModel());
//        ethEntityQueryWrapper.eq("device_address", ethEntity.getDeviceAddress());
//        List<EthEntity> list = this.list(ethEntityQueryWrapper);
//        if (list.size() > 0) {
//            return false;
//        } else {
//            if (ethEntity.getEthName() != null && ethEntity.getTargetIp() != null && ethEntity.getTargetPort() != null && ethEntity.getLocalPort() != null) {
//                this.save(ethEntity);
//                return true;
//            } else {
//                return false;
//            }
//
//
//        }

    }
}
