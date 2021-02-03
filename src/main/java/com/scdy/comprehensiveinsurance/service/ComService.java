package com.scdy.comprehensiveinsurance.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.scdy.comprehensiveinsurance.entity.ComEntity;
import com.scdy.comprehensiveinsurance.model.PageModel;

/**
 * <p>
 * 串口服务类
 * </p>
 *
 * @author zl
 * @since 2020-11-04
 */
public interface ComService extends IService<ComEntity> {


    /**
     * 保存串口数据.
     *
     * @param comEntity
     * @return
     */
    boolean saveComData(ComEntity comEntity);

    /**
     * 串口列表.
     * 根据页码获取串口列表数据
     *
     *
     * @param pageIndex 页码
     * @param pageSize 返回条数
     * @return
     */
    PageModel<ComEntity> getComDataList(int pageIndex, int pageSize);


}
