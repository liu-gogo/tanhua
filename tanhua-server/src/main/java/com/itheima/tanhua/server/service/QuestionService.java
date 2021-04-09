package com.itheima.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.itheima.tanhua.server.mapper.QuestionMapper;
import com.itheima.tanhua.server.pojo.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;


    public Question queryQuestion(Long userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", userId);
        return this.questionMapper.selectOne(queryWrapper);
    }
}