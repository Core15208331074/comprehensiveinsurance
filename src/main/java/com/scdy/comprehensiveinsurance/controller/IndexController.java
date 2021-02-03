package com.scdy.comprehensiveinsurance.controller;

import org.influxdb.InfluxDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @description: 前端控制器
 * @author: xu zhihao
 * @create: 2019-06-14 10:36
 */
@Controller
public class IndexController {
    @Autowired
    private InfluxDB influxDB;

    /**
     * 跳转到首页
     *
     * @return
     */
    @GetMapping("")
    public String index() {
        return "index";
    }


}
