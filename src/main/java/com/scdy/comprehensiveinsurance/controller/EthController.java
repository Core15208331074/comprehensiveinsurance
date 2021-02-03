package com.scdy.comprehensiveinsurance.controller;

import com.scdy.comprehensiveinsurance.entity.EthEntity;
import com.scdy.comprehensiveinsurance.model.ComRequestModel;
import com.scdy.comprehensiveinsurance.model.PageModel;
import com.scdy.comprehensiveinsurance.model.SensorModel;
import com.scdy.comprehensiveinsurance.model.electronicFence.ClothRemovalSetModel;
import com.scdy.comprehensiveinsurance.model.lighting.LightingModel;
import com.scdy.comprehensiveinsurance.response.ResponseData;
import com.scdy.comprehensiveinsurance.response.ResponseDataUtil;
import com.scdy.comprehensiveinsurance.service.EthService;
import com.scdy.comprehensiveinsurance.service.antiControl.ElectronicFenceService;
import com.scdy.comprehensiveinsurance.service.antiControl.EnvControlLitongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * eth控制层
 */
@Controller
@RequestMapping("eth")
public class EthController {
    @Autowired
    private EthService ethService;
    @Autowired
    private ElectronicFenceService electronicFenceService;
    @Autowired
    private EnvControlLitongService envControlLitongService;


    /**
     * eth 环控(利通)照明控制
     *
     * @param lightingModel
     * @return
     */
    @PostMapping("/lightingControl")
    @ResponseBody
    public ResponseData lightingControl(@RequestBody LightingModel lightingModel) {
        try {
            envControlLitongService.lightingOperation(lightingModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseDataUtil.buildSuccess();
    }

    /**
     * eth布撤防设置
     *
     * @param clothRemovalSetModel
     * @return
     */
    @PostMapping("/clothRemovalSet")
    @ResponseBody
    public ResponseData clothRemovalSet(@RequestBody ClothRemovalSetModel clothRemovalSetModel) {
        SensorModel sensorModel = null;
        try {
            sensorModel = electronicFenceService.clothRemovalSet(clothRemovalSetModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseDataUtil.buildSuccess(sensorModel);
    }

    /**
     * eth传感器产生的所有数据
     *
     * @param comRequestModel
     * @return
     */
    @GetMapping("/getSensorDataPage")
    @ResponseBody
    public ResponseData getSensorDataPage(ComRequestModel comRequestModel) {
        PageModel<SensorModel> sensorPageData = null;
        try {
            sensorPageData = ethService.getSensorDataPage(comRequestModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseDataUtil.buildSuccess(sensorPageData);
    }
//    /**
//     * eth传感器产生的所有数据
//     *
//     * @param id     eth行id
//     * @param pageIndex 页码
//     * @param pageSize  每页查询多少条
//     * @return
//     */
//    @GetMapping("/getSensorDataPage")
//    @ResponseBody
//    public ResponseData getSensorDataPage(String id, int pageIndex, int pageSize) {
//        PageModel<SensorModel> sensorPageData = null;
//        try {
//            sensorPageData = ethService.getSensorDataPage(id, pageIndex, pageSize);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return ResponseDataUtil.buildSuccess(sensorPageData);
//    }


    /**
     * eth传感器的实时数据
     *
     * @param id eth行id
     * @return
     */
    @GetMapping("/getRealtimeData")
    @ResponseBody
    public ResponseData getRealtimeData(String id) {
        SensorModel json = null;
        try {
            json = ethService.getRealtimeData(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseDataUtil.buildSuccess(json);
    }

    /**
     * 操作eth传感器
     *
     * @param id      eth信息id
     * @param isStart 启动/关闭
     * @return
     */
    @PostMapping("/isStartById")
    @ResponseBody
    public ResponseData isStartById(String id, Boolean isStart) {
        HashMap<String, Object> map = new HashMap<>();
        try {
            map.put("ethId", id);
            map.put("isStart", ethService.isStartById(id, isStart));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseDataUtil.buildSuccess(map);
    }


    /**
     * eth传感器列表
     *
     * @param pageIndex 页码
     * @param pageSize  数量
     * @return
     */
    @GetMapping("getEthList")
    @ResponseBody
    public ResponseData getEthList(int pageIndex, int pageSize) {
        PageModel<EthEntity> ethList = ethService.getEthList(pageIndex, pageSize);
        return ResponseDataUtil.buildSuccess(ethList);
    }

    /**
     * 删除eth传感器
     *
     * @param id
     * @return
     */
    @DeleteMapping("delEthById")
    @ResponseBody
    public ResponseData delEthById(String id) {
        try {
            ethService.isStartById(id, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean b = ethService.removeById(id);
        HashMap<String, Boolean> stringBooleanHashMap = new HashMap<>();
        stringBooleanHashMap.put("result", b);
        return ResponseDataUtil.buildSuccess(stringBooleanHashMap);
    }

    /**
     * 添加eth传感器
     *
     * @param ethEntity
     * @return
     */
    @PostMapping("saveEth")
    @ResponseBody
    public ResponseData saveEth(EthEntity ethEntity) {
        boolean b = ethService.saveEth(ethEntity);
        HashMap<String, Boolean> stringBooleanHashMap = new HashMap<>();
        stringBooleanHashMap.put("result", b);
        return ResponseDataUtil.buildSuccess(stringBooleanHashMap);
    }

    /**
     * eth传感器配置
     *
     * @return
     */
    @GetMapping("getConfig")
    @ResponseBody
    public ResponseData getConfig() {
        Map<String, Object> configMap = ethService.getConfig();
        return ResponseDataUtil.buildSuccess(configMap);
    }


}
