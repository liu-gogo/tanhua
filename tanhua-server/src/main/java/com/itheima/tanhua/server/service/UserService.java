package com.itheima.tanhua.server.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.tanhua.dubbo.server.api.UserLikeApi;
import com.itheima.tanhua.dubbo.server.api.VisitorsApi;
import com.itheima.tanhua.dubbo.server.pojo.RecommendUser;
import com.itheima.tanhua.dubbo.server.pojo.UserLike;
import com.itheima.tanhua.dubbo.server.pojo.Visitors;
import com.itheima.tanhua.server.utils.UserThreadLocal;
import com.itheima.tanhua.server.vo.PageResult;
import com.itheima.tanhua.server.vo.UserInfoVo;
import com.itheima.tanhua.server.vo.UserLikeListVo;
import com.itheima.tanhua.sso.enums.SexEnum;
import com.itheima.tanhua.sso.pojo.User;
import com.itheima.tanhua.sso.pojo.UserInfo;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private SSOService ssoService;

    public UserInfoVo queryUserInfo(Long userId, Long huanxinID) {
        User user = UserThreadLocal.get();
        Long userID = user.getId();
        if (huanxinID != null){
            userID = huanxinID;
        }else if(userId != null){
            userID = userId;
        }
        UserInfo userInfo = ssoService.findUserInfoByUserId(userID);
        if (userInfo == null){
            return null;
        }
        UserInfoVo userInfoVo = new UserInfoVo();
        userInfoVo.setAge(userInfo.getAge() != null ? userInfo.getAge().toString() : null);
        userInfoVo.setAvatar(userInfo.getLogo());
        userInfoVo.setBirthday(userInfo.getBirthday());
        userInfoVo.setEducation(userInfo.getEdu());
        userInfoVo.setCity(userInfo.getCity());
        userInfoVo.setGender(userInfo.getSex().name().toLowerCase());
        userInfoVo.setId(userInfo.getUserId());
        userInfoVo.setIncome(userInfo.getIncome() + "K"); //13K
        userInfoVo.setMarriage(StringUtils.equals(userInfo.getMarriage(), "已婚") ? 1 : 0);
        userInfoVo.setNickname(userInfo.getNickName());
        userInfoVo.setProfession(userInfo.getIndustry());

        return userInfoVo;
    }

    public Boolean updateUserInfo(UserInfoVo userInfoVo) {
        UserInfo userInfo = new UserInfo();
        userInfo.setNickName(userInfo.getNickName());
        userInfo.setUserId(UserThreadLocal.get().getId());

        if (StringUtils.endsWithIgnoreCase("man",userInfoVo.getGender())){
            userInfo.setSex(SexEnum.MAN);
        }else{
            userInfo.setSex(SexEnum.WOMAN);
        }
        userInfo.setCity(userInfoVo.getCity());
        userInfo.setBirthday(userInfoVo.getBirthday());

        return this.ssoService.updateUserInfo(userInfo);

    }

    @Reference(version = "1.0.0")
    private UserLikeApi userLikeApi;


    public Map<String, Long> counts() {
        User user = UserThreadLocal.get();

        Map<String,Long> result = new HashMap<>();
        result.put("eachLoveCount",userLikeApi.queryEachLikeCount(user.getId()));
        result.put("loveCount",userLikeApi.queryLikeCount(user.getId()));
        result.put("fanCount",userLikeApi.queryFanCount(user.getId()));

        return result;

    }

    @Reference(version = "1.0.0")
    private VisitorsApi visitorsApi;

    @Autowired
    private RecommendUserService recommendUserService;


    public PageResult friends(int type, int page, int pageSize, String nickname) {

        User user = UserThreadLocal.get();
        Long userId = user.getId();
        List<Long> friendIdList = new ArrayList<>();

        switch (type){
            case 1:{
                //互相喜欢
                List<UserLike> userLikeList = this.userLikeApi.queryEachLikeList(userId,page,pageSize);
                for (UserLike userLike : userLikeList) {
                    friendIdList.add(userLike.getUserId());
                }
                break;
            }
            case 2:{
                //喜欢
                List<UserLike> userLikeList = this.userLikeApi.queryLikeList(userId,page,pageSize);
                for (UserLike userLike : userLikeList) {
                    friendIdList.add(userLike.getLikeUserId());
                }
                break;
            }
            case 3:{
                //粉丝
                List<UserLike> userLikeList = this.userLikeApi.queryFanList(userId,page,pageSize);
                for (UserLike userLike : userLikeList) {
                    friendIdList.add(userLike.getUserId());
                }
                break;
            }
            case 4:{
                //谁看过我
                List<Visitors> visitorsList = visitorsApi.queryVisitorsPage(user.getId(), page, pageSize);
                for (Visitors visitors : visitorsList) {
                    friendIdList.add(visitors.getVisitorUserId());
                }
                break;
            }
            default:{
                break;
            }
        }


        PageResult pageResult = new PageResult();
        pageResult.setPages(0);
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);
        pageResult.setCounts(0);
        if (CollectionUtils.isEmpty(friendIdList)){
            return pageResult;
        }
        List<UserLikeListVo> userLikeListVoList = new ArrayList<>();

        //in ()
        List<UserInfo> userInfoList = this.ssoService.findUserInfoList(friendIdList, nickname);

        for (UserInfo userInfo : userInfoList) {
            UserLikeListVo userLikeListVo = new UserLikeListVo();
            userLikeListVo.setAge(userInfo.getAge());
            userLikeListVo.setAvatar(userInfo.getLogo());
            userLikeListVo.setCity(userInfo.getCity());
            userLikeListVo.setEducation(userInfo.getEdu());
            userLikeListVo.setGender(userInfo.getSex().name().toLowerCase());
            userLikeListVo.setId(userInfo.getUserId());
            userLikeListVo.setMarriage(StringUtils.equals(userInfo.getMarriage(), "已婚") ? 1 : 0);
            userLikeListVo.setNickname(userInfo.getNickName());


            RecommendUser recommendUser = this.recommendUserService.findRecommendUser(user.getId(),userInfo.getUserId());
            if (recommendUser == null){
                userLikeListVo.setMatchRate(RandomUtils.nextInt(60,99));
            }else{
                userLikeListVo.setMatchRate(recommendUser.getScore().intValue());
            }

            Boolean like = this.userLikeApi.isLike(userInfo.getUserId(), user.getId());
            userLikeListVo.setAlreadyLove(like);

            userLikeListVoList.add(userLikeListVo);
        }
        pageResult.setItems(userLikeListVoList);
        return pageResult;
    }
}
