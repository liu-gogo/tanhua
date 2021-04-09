package com.itheima.tanhua.dubbo.server.api;



import com.itheima.tanhua.dubbo.server.pojo.Visitors;

import java.util.List;

public interface VisitorsApi {

    /**
     * 保存来访记录
     *
     * @param visitors
     * @return
     */
    String saveVisitor(Visitors visitors);
    /**
     * 查询访客信息列表
     * @param userId
     * @param date  如果是第一次查询 date为0
     * @param num  数量做为分页的pageSize
     * @return
     */
    List<Visitors> queryVisitorsList(Long userId,Long date,int num);

    List<Visitors> queryVisitorsPage(Long id, int page, int pageSize);
}
