package com.scdy.comprehensiveinsurance.controller;

import com.scdy.comprehensiveinsurance.entity.ComEntity;
import com.scdy.comprehensiveinsurance.model.ComRequestModel;
import com.scdy.comprehensiveinsurance.model.PageModel;
import com.scdy.comprehensiveinsurance.model.SensorModel;
import com.scdy.comprehensiveinsurance.response.ResponseData;
import com.scdy.comprehensiveinsurance.response.ResponseDataUtil;
import com.scdy.comprehensiveinsurance.response.ResultEnums;
import com.scdy.comprehensiveinsurance.service.ComOperationService;
import com.scdy.comprehensiveinsurance.service.DictService;
import com.scdy.comprehensiveinsurance.service.SensorService;
import com.scdy.comprehensiveinsurance.service.ComService;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Controller
@RequestMapping("/com")
public class ComCtroller {
    @Autowired
    private DictService dictService;
    @Autowired
    private ComOperationService comOperationService;
    @Autowired
    private SensorService sensorService;
    @Autowired
    private ComService comService;
    @Autowired
    private InfluxDB influxDB;
    @Value("${spring.influx.database:comprehensiveinsurance}")
    private String database;


    /**
     * com传感器产生的所有数据
     *
     * @param comRequestModel  请求对象
     * @return
     */
    @GetMapping("/getSensorPageData")
    @ResponseBody
    public ResponseData getSensorPageData(ComRequestModel comRequestModel) {
        PageModel<SensorModel> sensorPageData = null;
        try {
            sensorPageData = comOperationService.getSensorPageData(comRequestModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseDataUtil.buildSuccess(sensorPageData);
    }


    /**
     * com传感器产生的所有数据
     *
     * @param comId     串口id
     * @param pageIndex 页码
     * @param pageSize  每页查询多少条
     * @return
     */
//    @GetMapping("/getSensorPageData")
//    @ResponseBody
//    public ResponseData getSensorPageData(String comId, int pageIndex, int pageSize) {
//        PageModel<SensorModel> sensorPageData = null;
//        try {
//            sensorPageData = comOperationService.getSensorPageData(comId, pageIndex, pageSize);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return ResponseDataUtil.buildSuccess(sensorPageData);
//    }





    /**
     * com传感器实时数据
     *
     * @param comId 串口id
     * @return
     */
    @GetMapping("/getSensorRealtimeDataByComId")
    @ResponseBody
    public ResponseData getSensorRealtimeDataByComId(String comId) {
        SensorModel json = null;
        try {
            json = comOperationService.getSensorRealtimeDataByComId(comId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseDataUtil.buildSuccess(json);
    }

    /**
     * 查看时序数据库里面所有数据
     *
     * @return
     */
    @GetMapping("/findAllSensorModel")
    @ResponseBody
    public ResponseData findAllSensorModel() {
        QueryResult rs = influxDB.query(new Query("select * from sensorModel  ", database));
        return ResponseDataUtil.buildSuccess(rs.getResults());
    }

    /**
     * 操作com传感器
     *
     * @param id      串口信息id
     * @param isStart 启动/关闭
     * @return
     */
    @PostMapping("/startOrStopComById")
    @ResponseBody
    public ResponseData startOrStopComById(String id, Boolean isStart) {
        HashMap<String, Object> map = new HashMap<>();
        try {
            map.put("id", id);
            map.put("isStart", comOperationService.startOrStopComById(id, isStart));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseDataUtil.buildSuccess(map);
    }

    /**
     * 删除com传感器
     *
     * @param id
     * @return
     */
    @DeleteMapping("/delComDataById")
    @ResponseBody
    public ResponseData delComDataById(String id) {
        ComEntity byId = comService.getById(id);
        if (!StringUtils.isEmpty(byId)) {
            try {
                comOperationService.startOrStopComById(id, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean b = comService.removeById(id);
            if (b) {
                return ResponseDataUtil.buildSuccess("删除成功");
            }
        }
        return ResponseDataUtil.buildError("删除失败");
    }

    /**
     * com传感器列表
     *
     * @param pageIndex 页码
     * @param pageSize  查询多少条
     * @return
     */
    @GetMapping("/getComDataList")
    @ResponseBody
    public ResponseData getComDataList(int pageIndex, int pageSize) {
        PageModel<ComEntity> comDataList = comService.getComDataList(pageIndex, pageSize);
        return ResponseDataUtil.buildSuccess(comDataList);
    }


    /**
     * 跳转到串口首页
     *
     * @return
     */
//    @GetMapping("/toIndex")
//    public String comIndex() {
//        return "com/comIndex";
//    }

    /**
     * 跳转到串口添加页
     *
     * @return
     */
//    @GetMapping("/toAdd")
//    public String toAadd() {
//        return "com/comAdd";
//    }

    /**
     * com传感器配置
     *
     * @return
     */
    @GetMapping("/getComConfigData")
    @ResponseBody
    public ResponseData getComConfigData() {
        HashMap<String, Object> map = comOperationService.getComConfigData();
        return ResponseDataUtil.buildSuccess(map);
    }

    /**
     * 添加com传感器
     *
     * @param comEntity
     * @return
     */
    @PostMapping("/comDataAdd")
    @ResponseBody
    public ResponseData comDataAdd(@RequestBody ComEntity comEntity) {
        boolean resultFlag = comService.saveComData(comEntity);
        if (resultFlag) {
            return ResponseDataUtil.buildSuccess(ResultEnums.SUCCESS_SAVE);
        }
//        return ResponseDataUtil.buildError(ResultEnums.FAIL_SAVE);
        return null;
    }


}
