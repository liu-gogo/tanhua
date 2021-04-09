package com.itheima.tanhua.dubbo.server.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageInfo<T> implements Serializable {

    private List<T> list;

    private long count;
}
