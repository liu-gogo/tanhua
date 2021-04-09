package com.itheima.tanhua.sso.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.itheima.tanhua.sso.mapper.UserInfoMapper;
import com.itheima.tanhua.sso.pojo.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInfoService {
    @Autowired
    private UserInfoMapper userInfoMapper;


    public UserInfo findUserInfoByUserId(long userId){
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.last("limit 1");

        return userInfoMapper.selectOne(queryWrapper);

    }

    public List<UserInfo> findUserInfoList(List<Long> userIdList, Integer age, String city, String education, String gender) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIdList);

        return userInfoMapper.selectList(queryWrapper);
    }

    public List<UserInfo> findUserInfoList(List<Long> userIdList, String keyword) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id",userIdList);
        if (StringUtils.isNotEmpty(keyword)){
            queryWrapper.like("nick_name",keyword);
        }
        return userInfoMapper.selectList(queryWrapper);
    }

    public Boolean updateUserInfo(UserInfo userInfo) {
        UpdateWrapper<UserInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id",userInfo.getUserId());
        int update = this.userInfoMapper.update(userInfo, updateWrapper);
        return update > 0;
    }
}
