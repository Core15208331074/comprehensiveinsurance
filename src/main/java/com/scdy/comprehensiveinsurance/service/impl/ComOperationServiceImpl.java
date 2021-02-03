package com.scdy.comprehensiveinsurance.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.Scheduler;
import cn.hutool.cron.TaskExecutor;
import cn.hutool.cron.listener.TaskListener;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scdy.comprehensiveinsurance.constant.GlobalConstants;
import com.scdy.comprehensiveinsurance.drive.com.SlaveOperation;
import com.scdy.comprehensiveinsurance.entity.ComEntity;
import com.scdy.comprehensiveinsurance.entity.DictEntity;
import com.scdy.comprehensiveinsurance.entity.SensorEntity;
import com.scdy.comprehensiveinsurance.model.ComRequestModel;
import com.scdy.comprehensiveinsurance.model.ComStatusModel;
import com.scdy.comprehensiveinsurance.model.PageModel;
import com.scdy.comprehensiveinsurance.model.SensorModel;
import com.scdy.comprehensiveinsurance.service.*;
import com.scdy.comprehensiveinsurance.service.analysis.impl.ComProtocolAnalysisServiceImpl;
import com.scdy.comprehensiveinsurance.utils.InfluxDBUtil;
import com.scdy.comprehensiveinsurance.utils.SerialPortManagerUtil;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 物理串口.
 */
@Service
@Transactional
public class ComOperationServiceImpl implements ComOperationService {
    @Autowired
    private ComService comService;
    @Value("${spring.influx.database:comprehensiveinsurance}")
    private String database;

    @Autowired
    private InfluxDB influxDB;
    @Autowired
    private ComProtocolAnalysisServiceImpl comProtocolAnalysisService;
    @Autowired
    private SensorDataService sensorDataService;
    @Autowired
    private DictService dictService;
    @Autowired
    private SensorService sensorService;


    @Override
    public PageModel<SensorModel> getSensorPageData(ComRequestModel comRequestModel) throws Exception {
        String comId = comRequestModel.getId();
        Date startTime = comRequestModel.getStartTime();
        Date endTime = comRequestModel.getEndTime();
        int pageIndex = comRequestModel.getPageIndex();
        int pageSize = comRequestModel.getPageSize();

        PageModel<SensorModel> sensorModelPageModel = new PageModel<>();
        sensorModelPageModel.setCurrentPage(pageIndex);
        sensorModelPageModel.setPageSize(pageSize);

        QueryWrapper<ComEntity> serialPortEntityQueryWrapper = new QueryWrapper<>();
        serialPortEntityQueryWrapper.eq("is_deleted", 0);
        serialPortEntityQueryWrapper.eq("id", comId);
        List<ComEntity> comEntityList = comService.list(serialPortEntityQueryWrapper);
        ComEntity comEntity1 = comEntityList.get(0);

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

        String mysql1 = "select * from sensorModel  where  isControl='0' and  " +
                "type='" + GlobalConstants.COM + "'" +
                startTimeStr + endTimeStr +
                " and sensorNameId ='" + comEntity1.getSensorModel() + "' ORDER BY time DESC limit " + pageSize + " offset " + (pageIndex - 1) * pageSize;
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


        String mysql2 = "select COUNT(json)  from sensorModel  where  isControl='0' and  " +
                "type='" + GlobalConstants.COM + "'" +
                startTimeStr + endTimeStr +
                " and  sensorNameId ='" + comEntity1.getSensorModel() + "'";

        QueryResult query = influxDB.query(new Query(mysql2, database));
        List<Object> countList = InfluxDBUtil.getListByField(query, "count");
        for (Object count : countList) {
            Double aDouble = Double.parseDouble(count.toString());
            sensorModelPageModel.setTotalPage(aDouble.longValue());
        }
        return sensorModelPageModel;
    }

    @Override
    public PageModel<SensorModel> getSensorPageData(String comId, int pageIndex, int pageSize) throws Exception {

        PageModel<SensorModel> sensorModelPageModel = new PageModel<>();
        sensorModelPageModel.setCurrentPage(pageIndex);
        sensorModelPageModel.setPageSize(pageSize);

        QueryWrapper<ComEntity> serialPortEntityQueryWrapper = new QueryWrapper<>();
        serialPortEntityQueryWrapper.eq("is_deleted", 0);
        serialPortEntityQueryWrapper.eq("id", comId);
        List<ComEntity> comEntityList = comService.list(serialPortEntityQueryWrapper);
        ComEntity comEntity1 = comEntityList.get(0);
        QueryResult rs = influxDB.query(new Query("select * from sensorModel  where type='" + GlobalConstants.COM + "' and sensorNameId ='" + comEntity1.getSensorModel() + "' ORDER BY time DESC limit " + pageSize + " offset " + (pageIndex - 1) * pageSize, database));
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

        QueryResult query = influxDB.query(new Query("select COUNT(json)  from sensorModel  where type='" + GlobalConstants.COM + "' and  sensorNameId ='" + comEntity1.getSensorModel() + "'", database));
        List<Object> countList = InfluxDBUtil.getListByField(query, "count");
        for (Object count : countList) {
            Double aDouble = Double.parseDouble(count.toString());
            sensorModelPageModel.setTotalPage(aDouble.longValue());
        }
        return sensorModelPageModel;
    }

    @Override
    public SensorModel getSensorRealtimeDataByComId(String comId) throws Exception {
        QueryWrapper<ComEntity> serialPortEntityQueryWrapper = new QueryWrapper<>();
        serialPortEntityQueryWrapper.eq("is_deleted", 0);
        serialPortEntityQueryWrapper.eq("id", comId);
        List<ComEntity> comEntityList = comService.list(serialPortEntityQueryWrapper);
        ComEntity comEntity1 = comEntityList.get(0);
        QueryResult rs = influxDB.query(new Query("select * from sensorModel  where  isControl='0' and  type='" + GlobalConstants.COM + "' and sensorNameId ='" + comEntity1.getSensorModel() + "' ORDER BY time DESC limit 1", database));
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



    @Override
    public List<String> getComList() {
        ArrayList<String> portList = SerialPortManagerUtil.findPort();
        return portList;
    }

    @Override
    public Boolean startOrStopComById(String serialPortId, boolean isStart) throws Exception {
        Boolean aBoolean = null;
        if (isStart) {
            //启动
            aBoolean = startComById(serialPortId);
        } else {
            //关闭
            aBoolean = stopComById(serialPortId);
        }
        return aBoolean;
    }

    @Override
    public Boolean startComById(String serialPortId) throws Exception {
        ComEntity comEntity = comService.getById(serialPortId);
        if (!StringUtils.isEmpty(comEntity)) {

            LinkedHashMap<String, LinkedHashMap<String, ComStatusModel>> comStatusMap = GlobalConstants.getComStatusMap();
            LinkedHashMap<String, ComStatusModel> stringComStatusModelLinkedHashMap1 = comStatusMap.get(comEntity.getSerialPortName());

            ComStatusModel comStatusModel = new ComStatusModel();
            //构建请求指令
            SensorModel sensorModel = sensorDataService.getSensorSendData(comEntity);
            comStatusModel.setSensorModel(sensorModel);
            comStatusModel.setComEntity(comEntity);
            comStatusModel.setIsStart(true);

            if (StringUtils.isEmpty(stringComStatusModelLinkedHashMap1)) {
                //没找到
                LinkedHashMap<String, ComStatusModel> stringComStatusModelLinkedHashMap = new LinkedHashMap<>();
                stringComStatusModelLinkedHashMap.put(comEntity.getDeviceAddress(), comStatusModel);
                comStatusMap.put(comEntity.getSerialPortName(), stringComStatusModelLinkedHashMap);

                ThreadUtil.execAsync(new Runnable() {
                    @Override
                    public void run() {
/*
                        LinkedHashMap<String, LinkedHashMap<String, ComStatusModel>> comStatusMap1 = GlobalConstants.getComStatusMap();
                        LinkedHashMap<String, ComStatusModel> stringComStatusModelLinkedHashMap1 = comStatusMap1.get(comEntity.getSerialPortName());

                        for (Map.Entry<String, ComStatusModel> entry : stringComStatusModelLinkedHashMap1.entrySet()) {
                            String address = entry.getKey();//地址
                            ComStatusModel comStatusModel1 = entry.getValue();//串口状态
                            if (comStatusModel1.getIsStart()) {
                                SensorModel sensorModel1 = comStatusModel1.getSensorModel();
                                ComEntity serialPortEntity1 = comStatusModel1.getComEntity();
                                ComThread comThread = new ComThread(serialPortEntity1, sensorModel1);
                                comThread.startRun();
                            }
                        }
*/

                        String schedule = CronUtil.schedule("1/10 * * * * ?", new Task() {
                            @Override
                            public void execute() {
                                LinkedHashMap<String, LinkedHashMap<String, ComStatusModel>> comStatusMap1 = GlobalConstants.getComStatusMap();
                                LinkedHashMap<String, ComStatusModel> stringComStatusModelLinkedHashMap1 = comStatusMap1.get(comEntity.getSerialPortName());

                                for (Map.Entry<String, ComStatusModel> entry : stringComStatusModelLinkedHashMap1.entrySet()) {
                                    String address = entry.getKey();//地址
                                    ComStatusModel comStatusModel1 = entry.getValue();//串口状态
                                    if (comStatusModel1.getIsStart()) {
                                        SensorModel sensorModel1 = comStatusModel1.getSensorModel();
                                        ComEntity comEntity1 = comStatusModel1.getComEntity();
                                        SlaveOperation comThread = new SlaveOperation(comEntity1, sensorModel1);
                                        try {
                                            comThread.startRun();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        });

                        // 支持秒级别定时任务
                        CronUtil.setMatchSecond(true);
                        CronUtil.start();

                        Scheduler scheduler = CronUtil.getScheduler();
                        scheduler.addListener(new TaskListener() {
                            @Override
                            public void onStart(TaskExecutor taskExecutor) {
                            }

                            @Override
                            public void onSucceeded(TaskExecutor taskExecutor) {
                            }

                            @Override
                            public void onFailed(TaskExecutor taskExecutor, Throwable throwable) {
                                CronUtil.getScheduler().clear();
                            }
                        });


//                        while (true) {
//                            LinkedHashMap<String, LinkedHashMap<String, ComStatusModel>> comStatusMap1 = GlobalConstants.getComStatusMap();
//                            LinkedHashMap<String, ComStatusModel> stringComStatusModelLinkedHashMap1 = comStatusMap1.get(comEntity.getSerialPortName());
//
//                            for (Map.Entry<String, ComStatusModel> entry : stringComStatusModelLinkedHashMap1.entrySet()) {
//                                String address = entry.getKey();//地址
//                                ComStatusModel comStatusModel1 = entry.getValue();//串口状态
//                                if (comStatusModel1.getIsStart()) {
//                                    SensorModel sensorModel1 = comStatusModel1.getSensorModel();
//                                    ComEntity serialPortEntity1 = comStatusModel1.getComEntity();
//                                    ComThread comThread = new ComThread(serialPortEntity1, sensorModel1);
//                                    comThread.startRun();
//                                }
//                                try {
//                                    Thread.sleep(1000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//
//                            if (GlobalConstants.getComStatusMap().size() < 1) {
//                                System.out.println("停止："+GlobalConstants.getComStatusMap().size());
//                                break;
//                            }
//                        }


                    }
                });

            } else {
                //找到
                stringComStatusModelLinkedHashMap1.put(comEntity.getDeviceAddress(), comStatusModel);

            }


            return true;
        }
        return false;
    }

    @Override
    public Boolean stopComById(String serialPortId) {
        ComEntity comEntity = comService.getById(serialPortId);
        if (!StringUtils.isEmpty(comEntity)) {

            LinkedHashMap<String, LinkedHashMap<String, ComStatusModel>> comStatusMap = GlobalConstants.getComStatusMap();
            LinkedHashMap<String, ComStatusModel> stringComStatusModelLinkedHashMap1 = comStatusMap.get(comEntity.getSerialPortName());
            if (StringUtils.isEmpty(stringComStatusModelLinkedHashMap1)) {
                //表示这个没启动
                return false;

            } else {
                //表示已经启动
                Iterator<String> iterator = stringComStatusModelLinkedHashMap1.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    if (comEntity.getDeviceAddress().equals(key)) {
                        iterator.remove();
                    }
                }

                if (stringComStatusModelLinkedHashMap1.size() < 1) {
                    Iterator<String> iterator1 = comStatusMap.keySet().iterator();
                    while (iterator1.hasNext()) {
                        String key = iterator1.next();
                        if (comEntity.getSerialPortName().equals(key)) {
                            iterator1.remove();
                        }
                    }

                }
            }
            return false;
        }
        return null;
    }

    @Override
    public HashMap<String, Object> getComConfigData() {
        HashMap<String, Object> map = new HashMap<>();
        //串口
        List<String> comList = this.getComList();
        map.put("comList", comList);
        //波特率
        List<DictEntity> baudRateList = dictService.list(new QueryWrapper<DictEntity>().eq("name", "baudRate"));
        map.put("baudRateList", baudRateList);
        //校验位
        List<DictEntity> checkDigitList = dictService.list(new QueryWrapper<DictEntity>().eq("name", "checkDigit"));
        map.put("checkDigitList", checkDigitList);
        //数据位
        List<DictEntity> dataBitList = dictService.list(new QueryWrapper<DictEntity>().eq("name", "dataBit"));
        map.put("dataBitList", dataBitList);
        //停止位
        List<DictEntity> stopBitList = dictService.list(new QueryWrapper<DictEntity>().eq("name", "stopBit"));
        map.put("stopBitList", stopBitList);
        //传感器型号
        QueryWrapper<SensorEntity> sensorEntityQueryWrapper = new QueryWrapper<>();
        sensorEntityQueryWrapper.eq("is_deleted", 0);
        sensorEntityQueryWrapper.eq("type", "com");
        List<SensorEntity> sensorList = sensorService.list(sensorEntityQueryWrapper);
        map.put("sensorList", sensorList);
        return map;
    }
}
