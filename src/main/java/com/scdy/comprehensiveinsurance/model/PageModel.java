package com.scdy.comprehensiveinsurance.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageModel<T> implements Serializable {
    /**
     * 总页
     */
    public long totalPage;
    /**
     * 数据list
     */
    public List<T> datalist;
    /**
     * 当前页
     */
    public int currentPage;
    /**
     * 每页数
     */
    public int pageSize;

}
