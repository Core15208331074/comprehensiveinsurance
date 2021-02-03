package com.scdy.comprehensiveinsurance.utils;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scdy.comprehensiveinsurance.entity.DictEntity;
import com.scdy.comprehensiveinsurance.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DictUtil {
    @Autowired
    private DictService dictService;

    public String getDictValue(String type, String name) {

        QueryWrapper<DictEntity> dictEntityQueryWrapper1 = new QueryWrapper<>();
        dictEntityQueryWrapper1.eq("is_deleted", 0);
        dictEntityQueryWrapper1.eq("type", type);
        dictEntityQueryWrapper1.eq("name", name);
        DictEntity dictEntity = dictService.getOne(dictEntityQueryWrapper1);
        return dictEntity.getValue();
    }
}
