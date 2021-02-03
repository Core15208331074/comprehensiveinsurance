package com.scdy.comprehensiveinsurance.utils;

import org.influxdb.dto.QueryResult;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 时序数据库工具。
 */
public class InfluxDBUtil {


    /**
     * 根据字段查询结果list.
     *
     * @param queryResult 时序数据库返回的结果
     * @param field       需要查询的字段
     * @return
     */
    public static List<Object> getListByField(QueryResult queryResult, String field) {
        List<QueryResult.Result> results = queryResult.getResults();
        QueryResult.Result result = results.get(0);
        List<QueryResult.Series> series = result.getSeries();
        if (StringUtils.isEmpty(series)) {
            return null;
        }
        QueryResult.Series series1 = series.get(0);
        List<String> columns1 = series1.getColumns();
        int fieldIndex = columns1.indexOf(field);

        ArrayList<Object> fieldList = new ArrayList<>();

        List<List<Object>> valuesList = series1.getValues();
        for (List<Object> objectList : valuesList) {
            Object fieldValue = objectList.get(fieldIndex);
            fieldList.add(fieldValue);
        }
        return fieldList;
    }
}
