package com.itheima.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.tanhua.server.mapper.AnnouncementMapper;
import com.itheima.tanhua.server.pojo.Announcement;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;


    public IPage<Announcement> queryList(Integer pageNum,Integer pageSize){
        Page<Announcement> page = new Page<>(pageNum,pageSize);
        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created");


        return announcementMapper.selectPage(page, queryWrapper);
    }
}
