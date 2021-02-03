package com.scdy.comprehensiveinsurance.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scdy.comprehensiveinsurance.constant.GlobalConstants;
import com.scdy.comprehensiveinsurance.dao.ComMapper;
import com.scdy.comprehensiveinsurance.entity.ComEntity;
import com.scdy.comprehensiveinsurance.entity.SensorEntity;
import com.scdy.comprehensiveinsurance.model.ComStatusModel;
import com.scdy.comprehensiveinsurance.model.PageModel;
import com.scdy.comprehensiveinsurance.service.ComService;
import com.scdy.comprehensiveinsurance.service.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zl
 * @since 2020-11-04
 */
@Service
@Transactional
public class ComServiceImpl extends ServiceImpl<ComMapper, ComEntity> implements ComService {
    @Autowired
    private SensorService sensorService;




    @Override
    public boolean saveComData(ComEntity comEntity) {
        String serialPortName = comEntity.getSerialPortName();
        String deviceAddress = comEntity.getDeviceAddress();

        QueryWrapper<ComEntity> serialPortEntityQueryWrapper = new QueryWrapper<>();
        serialPortEntityQueryWrapper.eq("is_deleted", 0);
        serialPortEntityQueryWrapper.eq("serial_port_name", serialPortName);
        serialPortEntityQueryWrapper.eq("device_address", deviceAddress);
        List<ComEntity> SerialPortList = this.list(serialPortEntityQueryWrapper);
        if (SerialPortList.size() > 0) {
            return false;
        }

        boolean save = this.save(comEntity);
        return save;
    }

    @Override
    public PageModel<ComEntity> getComDataList(int pageIndex, int pageSize) {
        PageModel<ComEntity> serialPortEntityPageModel = new PageModel<>();

        QueryWrapper<ComEntity> serialPortEntityQueryWrapper = new QueryWrapper<>();
        serialPortEntityQueryWrapper.eq("is_deleted", 0);
        Page<ComEntity> page = new Page<>(pageIndex,pageSize);
        Page<ComEntity> page1 = this.page(page, serialPortEntityQueryWrapper);
        List<ComEntity> serialPortList = page1.getRecords();

        for (ComEntity comEntity : serialPortList) {
            String sensorModel = comEntity.getSensorModel();//传感器id
            QueryWrapper<SensorEntity> sensorEntityQueryWrapper = new QueryWrapper<>();
            sensorEntityQueryWrapper.eq("is_deleted", 0);
            sensorEntityQueryWrapper.eq("type", "com");
            sensorEntityQueryWrapper.eq("sensor_name_code", sensorModel);
            SensorEntity sensorEntity = sensorService.getOne(sensorEntityQueryWrapper);
            String sensorName = sensorEntity.getSensorName();
            comEntity.setSensorModel(sensorName);
//            Boolean aBoolean = GlobalConstants.getComStartStatusMap().get(comEntity.getSerialPortName());


            LinkedHashMap<String, LinkedHashMap<String, ComStatusModel>> comStatusMap = GlobalConstants.getComStatusMap();
            LinkedHashMap<String, ComStatusModel> stringComStatusModelLinkedHashMap = comStatusMap.get(comEntity.getSerialPortName());
           if(StringUtils.isEmpty(stringComStatusModelLinkedHashMap)){
               comEntity.setIsStart(false);
           }else {
               ComStatusModel comStatusModel = stringComStatusModelLinkedHashMap.get(comEntity.getDeviceAddress());
               Boolean isStart = comStatusModel.getIsStart();
               comEntity.setIsStart(isStart);
           }

        }

        serialPortEntityPageModel.setCurrentPage(pageIndex);
        serialPortEntityPageModel.setPageSize(pageSize);

        int count = this.count(serialPortEntityQueryWrapper);
        serialPortEntityPageModel.setTotalPage(count);
        serialPortEntityPageModel.setDatalist(serialPortList);

        return serialPortEntityPageModel;
    }

}
