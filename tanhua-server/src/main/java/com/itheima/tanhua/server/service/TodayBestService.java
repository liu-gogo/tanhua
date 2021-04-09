package com.itheima.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.tanhua.dubbo.server.api.UserLikeApi;
import com.itheima.tanhua.dubbo.server.api.UserLocationApi;
import com.itheima.tanhua.dubbo.server.api.VisitorsApi;
import com.itheima.tanhua.dubbo.server.pojo.RecommendUser;
import com.itheima.tanhua.dubbo.server.pojo.Visitors;
import com.itheima.tanhua.dubbo.server.vo.UserLocationVo;
import com.itheima.tanhua.server.dto.RecommendUserQueryParam;
import com.itheima.tanhua.server.pojo.Question;
import com.itheima.tanhua.server.utils.UserThreadLocal;
import com.itheima.tanhua.server.vo.NearUserVo;
import com.itheima.tanhua.server.vo.PageResult;
import com.itheima.tanhua.server.vo.TodayBest;

import com.itheima.tanhua.sso.pojo.User;
import com.itheima.tanhua.sso.pojo.UserInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TodayBestService {
    @Autowired
    private SSOService ssoService;

    @Autowired
    private RecommendUserService recommendUserService;

    @Reference(version = "1.0.0")
    private VisitorsApi visitorsApi;

    public TodayBest todayBest(String token) {

        /**
         * 1.token 校验，去sso工程进行校验 （http-restTemplate，dubbo调用，sso工程开放一个dubbo服务）
         * 2.token校验成功，当前登录的userId，去查询此用户推荐的用户列表中 分数最大的
         *      mongo表  recommend_user表中查询 dubbo调用 RecommendUserApi
         * 3. RecommendUser为空 给个默认推荐用户
         * 4. RecommendUser userId
         *
         * 5. TodayBest  根据userId去sso查询用户的详细信息+RecommendUser的score 组装为TodayBest对象
         */

        //1.token 校验，去sso工程进行校验 （http-restTemplate，dubbo调用，sso工程开放一个dubbo服务）
        User user = ssoService.checkToken(token);

        if(user == null){
            return null;
        }

        //2.token校验成功，当前登录的userId，去查询此用户推荐的用户列表中 分数最大的
        RecommendUser userMaxScore = recommendUserService.findRecommendUserMaxScore(user.getId());

        //3. RecommendUser为空 给个默认推荐用户
        if( userMaxScore == null){
            userMaxScore = new RecommendUser();
            userMaxScore.setUserId(2L);
            userMaxScore.setScore(98d);
        }

        //4. RecommendUser userId
        Long userId = userMaxScore.getUserId();


        //5. TodayBest  根据userId去sso查询用户的详细信息+RecommendUser的score 组装为TodayBest对象
        TodayBest todayBest = new TodayBest();
        todayBest.setId(userId);
        todayBest.setFateValue(userMaxScore.getScore().longValue());

        UserInfo userInfo = ssoService.findUserInfoByUserId(userId);
        if(userInfo != null){
            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setGender(userInfo.getSex().getValue() == 1 ?"man":"woman");
            todayBest.setAge(userInfo.getAge());
            todayBest.setTags(StringUtils.split(userInfo.getTags(),","));
        }
        return todayBest;
    }

    @Value("${tanhua.sso.default.users}")
    private String defaultUsers;

    public PageResult recommendation(String token,
                                     RecommendUserQueryParam recommendUserQueryParam) {

        /**
         * 1. 校验token
         * 2. 推荐列表 是从mongo表recommend_user查询的 dubbo服务 List<RecommendUser>
         * 3. 获取 userIdList
         * 4. List<TodayBest>
         * 5. 构建TodayBest要userInfo信息 根据userIdList去查询用户信息列表
         * 6. PageResult
         */
        PageResult pageResult = new PageResult();
        Integer page = recommendUserQueryParam.getPage();
        pageResult.setPage(page);
        Integer pagesize = recommendUserQueryParam.getPagesize();
        pageResult.setPagesize(pagesize);
        //总记录数和总页数 不需要 app端不需求
        pageResult.setCounts(0);
        pageResult.setPages(0);

        /*
        User user = ssoService.checkToken(token);
        if (user == null){
            return pageResult;
        }*/
        User user = UserThreadLocal.get();

        List<RecommendUser> recommendUserList = recommendUserService.queryRecommendUserList(user.getId(),page,pagesize);

        if (CollectionUtils.isEmpty(recommendUserList)){
            String[] recommendUserIdList = StringUtils.split(defaultUsers, ",");
            for (String userId : recommendUserIdList) {

                RecommendUser recommendUser = new RecommendUser();
                recommendUser.setUserId(Long.valueOf(userId));
                recommendUser.setScore(RandomUtils.nextDouble(70d, 98d));
                recommendUserList.add(recommendUser);
            }

            return  pageResult;
        }
        // 3. 获取 userIdList
        List<Long> userIdList = new ArrayList<>();
        //4. List<TodayBest>
        List<TodayBest> todayBestList = new ArrayList<>();
        for (RecommendUser recommendUser : recommendUserList) {
            userIdList.add(recommendUser.getUserId());
            TodayBest todayBest = new TodayBest();
            todayBest.setId(recommendUser.getUserId());
            todayBest.setFateValue(recommendUser.getScore().longValue());
            todayBestList.add(todayBest);
        }

        //5. 构建TodayBest要userInfo信息 根据userIdList去查询用户信息列表

        List<UserInfo> userInfoList = this.ssoService.findUserInfoList(userIdList,
                recommendUserQueryParam.getAge(),
                recommendUserQueryParam.getCity(),
                recommendUserQueryParam.getEducation(),
                recommendUserQueryParam.getGender());
        Map<Long,UserInfo> map = new HashMap<>();
        for (UserInfo userInfo : userInfoList) {
            map.put(userInfo.getUserId(),userInfo);
        }
        for (TodayBest todayBest : todayBestList) {
            UserInfo userInfo = map.get(todayBest.getId());
            if (userInfo != null) {
                todayBest.setAvatar(userInfo.getLogo());
                todayBest.setNickname(userInfo.getNickName());
                todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));
                todayBest.setGender(userInfo.getSex().getValue() == 1 ? "man" : "woman");
                todayBest.setAge(userInfo.getAge());
            }
        }
        pageResult.setItems(todayBestList);
        return pageResult;
    }


    public TodayBest personalInfo(Long userId) {
        TodayBest todayBest = new TodayBest();
        UserInfo userInfo = ssoService.findUserInfoByUserId(userId);
        todayBest.setAvatar(userInfo.getLogo());
        todayBest.setNickname(userInfo.getNickName());
        todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));
        todayBest.setGender(userInfo.getSex().getValue() == 1 ? "man" : "woman");
        todayBest.setAge(userInfo.getAge());
        //缘分值 只有recommend_user表中有

        User user = UserThreadLocal.get();
        RecommendUser recommendUser = this.recommendUserService.findRecommendUser(user.getId(),userId);
        if (recommendUser != null){
            todayBest.setFateValue(recommendUser.getScore().longValue());
        }else{
            todayBest.setFateValue(RandomUtils.nextLong(60,90));
        }

        Visitors visitors = new Visitors();
        visitors.setFrom("首页");
        visitors.setUserId(userId);
        visitors.setVisitorUserId(user.getId());

        this.visitorsApi.saveVisitor(visitors);
        return todayBest;
    }


    @Reference(version = "1.0.0")
    private UserLocationApi userLocationApi;
    /**
     * 搜附近
     * @param gender
     * @param distance
     * @return
     */
    public List<NearUserVo> nearBy(String gender, Long distance) {
        /**
         * 1. 获取到登录用户，通过dubbo接口获取当前的地理位置信息
         * 2. 根据地理位置信息 查询 附近的人
         * 3. List<UserLocationVo> 要的是用户信息，得到List<Long> userIdList
         * 4. 调用sso 获取userInfo信息
         * 5， 封装List<NearUserVo>
         */

        User user = UserThreadLocal.get();
        Long userId = user.getId();

        UserLocationVo userLocationVo = userLocationApi.queryUserLocation(userId);
        if (userLocationVo == null){
            return new ArrayList<>();
        }
        List<UserLocationVo> userLocationVoList = userLocationApi.queryUserLocationList(userLocationVo.getLongitude(), userLocationVo.getLatitude(), distance);
        //附近的人
        if (CollectionUtils.isEmpty(userLocationVoList)){
            return new ArrayList<>();
        }
        List<Long> userIdList = new ArrayList<>();

        for (UserLocationVo locationVo : userLocationVoList) {
            userIdList.add(locationVo.getUserId());
        }
        List<UserInfo> userInfoList = this.ssoService.findUserInfoListByGender(userIdList,gender);

        List<NearUserVo> nearUserVoList = new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {
            NearUserVo nearUserVo = new NearUserVo();
            nearUserVo.setAvatar(userInfo.getLogo());
            nearUserVo.setNickname(userInfo.getNickName());
            nearUserVo.setUserId(userInfo.getUserId());

            nearUserVoList.add(nearUserVo);
        }

        return nearUserVoList;
    }

    @Autowired
    private QuestionService questionService;

    public String queryQuestion(Long userId) {
        Question question = questionService.queryQuestion(userId);
        if (question != null){
            return question.getTxt();
        }
        return "默认问题";
    }


    private static final ObjectMapper mapper = new ObjectMapper();
    public boolean reply(Long userId, String reply) {

        /**
         * 调用sso服务 发送环信消息
         */

        //{"userId": "1","nickname":"黑马小妹","strangerQuestion": "你喜欢去看蔚蓝的大海还是去爬巍峨的高山？","reply": "我喜欢秋天的落叶，夏天的泉水，冬天的雪地，只要有你一切皆可~"}

        Long loginUserId = UserThreadLocal.get().getId();
        Map<String,String> param = new HashMap<>();
        param.put("userId",loginUserId.toString());
        UserInfo userInfoByUserId = this.ssoService.findUserInfoByUserId(loginUserId);
        param.put("nickname",userInfoByUserId.getNickName());
        Question question = this.questionService.queryQuestion(userId);
        if (question == null){
            param.put("strangerQuestion", "");
        }else {
            param.put("strangerQuestion", question.getTxt());
        }
        param.put("reply",reply);
        String msg = null;
        try {
            msg = mapper.writeValueAsString(param);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
        return this.ssoService.sendMsg(userId,"txt",msg);

    }


    public List<TodayBest> cards() {
        /**
         * 1. 校验token userId
         * 2. 推荐列表 是从mongo表recommend_user查询的 dubbo服务 List<RecommendUser>
         * 3. 获取 userIdList
         * 4. List<TodayBest>
         * 5. 构建TodayBest要userInfo信息 根据userIdList去查询用户信息列表
         * 6. PageResult
         */

        User user = UserThreadLocal.get();
        List<RecommendUser> recommendUserList = recommendUserService.queryRecommendUserList(user.getId(), 1, 100);

        if (CollectionUtils.isEmpty(recommendUserList)){
            String[] recommendUserIdList = StringUtils.split(defaultUsers, ",");
            for (String uId : recommendUserIdList) {
                RecommendUser recommendUser = new RecommendUser();
                recommendUser.setUserId(Long.parseLong(uId));
                recommendUser.setScore(RandomUtils.nextDouble(70d, 89d));
                recommendUserList.add(recommendUser);
            }
        }

        int max = 10;
        List<RecommendUser> endrecommendUserList = new ArrayList<>();

        if (recommendUserList.size() > max){
            for (int i = 0; i < max; i++) {
                int randomIndex = RandomUtils.nextInt(0, recommendUserList.size());
                RecommendUser recommendUser = getRecommend(randomIndex, recommendUserList.size(), endrecommendUserList, recommendUserList);
                endrecommendUserList.add(recommendUser);
            }
        }else {
            endrecommendUserList = recommendUserList;
        }


        List<Long> userIdList = new ArrayList<>();

        List<TodayBest> todayBestList = new ArrayList<>();

        for (RecommendUser recommendUser : endrecommendUserList) {
            userIdList.add(recommendUser.getUserId());
            TodayBest todayBest = new TodayBest();
            todayBest.setId(recommendUser.getUserId());
            todayBest.setFateValue(recommendUser.getScore().longValue());
            todayBestList.add(todayBest);
        }

        List<UserInfo> userInfoList = ssoService.findUserInfoList(userIdList);
        Map<Long,UserInfo> map = new HashMap<>();

        for (UserInfo userInfo : userInfoList) {
            map.put(userInfo.getUserId(), userInfo);
        }

        for (TodayBest todayBest : todayBestList) {
            UserInfo userInfo = map.get(todayBest.getId());
            if (userInfo != null){
                todayBest.setAvatar(userInfo.getLogo());
                todayBest.setNickname(userInfo.getNickName());
                todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));
                todayBest.setGender(userInfo.getSex().getValue() == 1 ? "man" : "woman");
                todayBest.setAge(userInfo.getAge());

            }
        }
        return todayBestList;
    }

    private RecommendUser getRecommend(int randomIndex, int max,
                                       List<RecommendUser> endRecommendUserList,
                                       List<RecommendUser> recommendUserList) {
        RecommendUser recommendUser = recommendUserList.get(randomIndex);
        if (endRecommendUserList.contains(recommendUser)){
            randomIndex = RandomUtils.nextInt(0, max);
            return getRecommend(randomIndex,max,endRecommendUserList,recommendUserList);
        }
        return recommendUser;
    }

    @Reference(version = "1.0.0")
    private UserLikeApi userLikeApi;

    @Autowired
    private ImService imService;


    public void love(Long likeUserId) {

        User user = UserThreadLocal.get();
        String saveUserLike = userLikeApi.saveUserLike(user.getId(), likeUserId);
        if (saveUserLike != null){
            Boolean mutualLike = userLikeApi.isMutualLike(user.getId(), likeUserId);
            if(mutualLike){
                //加好友
                imService.addContacts(likeUserId);
            }
        }
    }

    public Boolean removeLikeUser(Long likeUserId) {

        User user = UserThreadLocal.get();
        return userLikeApi.deleteUserLike(user.getId(),likeUserId);
    }
}
