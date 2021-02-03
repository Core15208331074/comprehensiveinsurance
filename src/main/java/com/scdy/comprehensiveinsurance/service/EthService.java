package com.scdy.comprehensiveinsurance.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.scdy.comprehensiveinsurance.entity.EthEntity;
import com.scdy.comprehensiveinsurance.model.ComRequestModel;
import com.scdy.comprehensiveinsurance.model.PageModel;
import com.scdy.comprehensiveinsurance.model.SensorModel;

import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zl
 * @since 2020-11-24
 */
public interface EthService extends IService<EthEntity> {
    /**
     *分页数据
     *
     * @param comRequestModel
     * @return
     */
    PageModel<SensorModel> getSensorDataPage(ComRequestModel comRequestModel) throws Exception;
    /**
     * 分页数据
     *
     * @param id     eth行id
     * @param pageIndex 页码
     * @param pageSize  每页查询多少条
     * @return
     */
    PageModel<SensorModel> getSensorDataPage(String id, int pageIndex, int pageSize);
    /**
     * 实时数据
     *
     * @param id eth行id
     * @return
     */
    SensorModel getRealtimeData(String id);

    /**
     * 操作eth
     *
     * @param ethId   eth信息id
     * @param isStart 启动/关闭
     * @return
     */
    boolean isStartById(String ethId, boolean isStart) throws Exception;

    /**
     * eth列表
     *
     * @param pageIndex
     * @param pageSize
     * @return
     */
    PageModel<EthEntity> getEthList(int pageIndex, int pageSize);

    /**
     * 获取eth配置
     *
     * @return
     */
    Map<String, Object> getConfig();

    /**
     * 保存eth数据
     *
     * @param ethEntity
     * @return
     */
    boolean saveEth(EthEntity ethEntity);

}
