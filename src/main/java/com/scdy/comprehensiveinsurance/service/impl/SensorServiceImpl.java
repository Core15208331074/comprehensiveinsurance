package com.scdy.comprehensiveinsurance.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scdy.comprehensiveinsurance.dao.SensorMapper;
import com.scdy.comprehensiveinsurance.entity.SensorEntity;
import com.scdy.comprehensiveinsurance.service.SensorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zl
 * @since 2020-11-05
 */
@Service
@Transactional
public class SensorServiceImpl extends ServiceImpl<SensorMapper, SensorEntity> implements SensorService {
}
