package com.itheima.tanhua.server.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itheima.tanhua.dubbo.server.api.QuanZiApi;
import com.itheima.tanhua.dubbo.server.api.UsersApi;
import com.itheima.tanhua.dubbo.server.pojo.Comment;
import com.itheima.tanhua.dubbo.server.pojo.Users;
import com.itheima.tanhua.dubbo.server.vo.CommentTypeEnum;
import com.itheima.tanhua.server.pojo.Announcement;
import com.itheima.tanhua.server.utils.UserThreadLocal;
import com.itheima.tanhua.server.vo.Contacts;
import com.itheima.tanhua.server.vo.MessageAnnouncement;
import com.itheima.tanhua.server.vo.MessageLike;
import com.itheima.tanhua.server.vo.PageResult;
import com.itheima.tanhua.sso.pojo.User;
import com.itheima.tanhua.sso.pojo.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImService {
    @Reference(version = "1.0.0")
    private UsersApi usersApi;

    @Autowired
    private SSOService ssoService;

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    public boolean addContacts(Long firendId) {

        User user = UserThreadLocal.get();
        Long userId = user.getId();

        Users users  = new Users();
        users.setUserId(userId);
        users.setFriendId(firendId);
        String usersId = usersApi.saveUser(users);

        if(StringUtils.isEmpty(usersId)){
            return false;
        }

        return ssoService.saveContacts(userId,firendId);
    }

    public PageResult contactsList(int page, int pageSize, String keyword) {
        /**
         * 查看当前登录用户的联系人 tanhua_users表去查询好友id列表
         */
        User user = UserThreadLocal.get();
        Long userId = user.getId();
        List<Users> usersList = new ArrayList<>();
        if (StringUtils.isNotEmpty(keyword)){
            usersList = this.usersApi.findAllUsersList(userId);
        }else{
            usersList = this.usersApi.findUsersListPage(userId,page,pageSize);
        }

        PageResult pageResult = new PageResult();
        pageResult.setCounts(0);
        pageResult.setPages(0);
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);
        if (CollectionUtils.isEmpty(usersList)){
            return pageResult;
        }
        List<Long> friendIdList = new ArrayList<>();
        for (Users users : usersList) {
            friendIdList.add(users.getFriendId());
        }
        //去用户信息列表
        List<Contacts> contactsList = new ArrayList<>();

        List<UserInfo> userInfoList = this.ssoService.findUserInfoList(friendIdList,keyword);

        for (UserInfo userInfo : userInfoList) {
            Contacts contacts = new Contacts();
            contacts.setAge(userInfo.getAge());
            contacts.setAvatar(userInfo.getLogo());
            contacts.setGender(userInfo.getSex().name().toLowerCase());
            contacts.setNickname(userInfo.getNickName());
            contacts.setUserId(String.valueOf(userInfo.getUserId()));
            contacts.setCity(StringUtils.substringBefore(userInfo.getCity(), "-"));

            contactsList.add(contacts);
        }

        pageResult.setItems(contactsList);
        return pageResult;
    }

    public PageResult likes(int page, int pageSize) {

        User user = UserThreadLocal.get();

        List<Comment> comments = quanZiApi.queryCommentListByPublishUserId(user.getId(), CommentTypeEnum.LIKE.getCode());
        PageResult pageResult = new PageResult();
        pageResult.setCounts(0);
        pageResult.setPages(0);
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);

        if (CollectionUtils.isEmpty(comments)){
            return pageResult;
        }


        List<Long> userIdList = new ArrayList<>();
        List<MessageLike> messageLikeList = new ArrayList<>();
        for (Comment comment : comments) {
            if (!userIdList.contains(comment.getUserId())) {
                userIdList.add(comment.getUserId());
            }

            MessageLike messageLike = new MessageLike();
            messageLike.setUserId(comment.getUserId());
            messageLike.setId(comment.getId().toString());
            messageLike.setCreateDate(new DateTime(comment.getCreated()).toString("yyyy-MM-dd HH:mm"));
            messageLikeList.add(messageLike);
        }

        List<UserInfo> userInfoList = this.ssoService.findUserInfoList(userIdList);
        Map<Long,UserInfo> map = new HashMap<>();
        for (UserInfo userInfo : userInfoList) {
            map.put(userInfo.getUserId(),userInfo);
        }

        for (MessageLike messageLike : messageLikeList) {
            UserInfo userInfo = map.get(messageLike.getUserId());
            if (userInfo != null){
                messageLike.setAvatar(userInfo.getLogo());
                messageLike.setNickname(userInfo.getNickName());
            }
        }
        pageResult.setItems(messageLikeList);
        return pageResult;
    }

    public PageResult loves(int page, int pageSize) {

        User user = UserThreadLocal.get();

        List<Comment> comments = quanZiApi.queryCommentListByPublishUserId(user.getId(), CommentTypeEnum.LOVE.getCode());
        PageResult pageResult = new PageResult();
        pageResult.setCounts(0);
        pageResult.setPages(0);
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);

        if (CollectionUtils.isEmpty(comments)){
            return pageResult;
        }


        List<Long> userIdList = new ArrayList<>();
        List<MessageLike> messageLikeList = new ArrayList<>();
        for (Comment comment : comments) {
            if (!userIdList.contains(comment.getUserId())) {
                userIdList.add(comment.getUserId());
            }

            MessageLike messageLike = new MessageLike();
            messageLike.setUserId(comment.getUserId());
            messageLike.setId(comment.getId().toString());
            messageLike.setCreateDate(new DateTime(comment.getCreated()).toString("yyyy-MM-dd HH:mm"));
            messageLikeList.add(messageLike);
        }

        List<UserInfo> userInfoList = this.ssoService.findUserInfoList(userIdList);
        Map<Long,UserInfo> map = new HashMap<>();
        for (UserInfo userInfo : userInfoList) {
            map.put(userInfo.getUserId(),userInfo);
        }

        for (MessageLike messageLike : messageLikeList) {
            UserInfo userInfo = map.get(messageLike.getUserId());
            if (userInfo != null){
                messageLike.setAvatar(userInfo.getLogo());
                messageLike.setNickname(userInfo.getNickName());
            }
        }
        pageResult.setItems(messageLikeList);
        return pageResult;
    }

    public PageResult comments(int page, int pageSize) {
        User user = UserThreadLocal.get();

        List<Comment> comments = quanZiApi.queryCommentListByPublishUserId(user.getId(), CommentTypeEnum.COMMENT.getCode());
        PageResult pageResult = new PageResult();
        pageResult.setCounts(0);
        pageResult.setPages(0);
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);

        if (CollectionUtils.isEmpty(comments)){
            return pageResult;
        }


        List<Long> userIdList = new ArrayList<>();
        List<MessageLike> messageLikeList = new ArrayList<>();
        for (Comment comment : comments) {
            if (!userIdList.contains(comment.getUserId())) {
                userIdList.add(comment.getUserId());
            }

            MessageLike messageLike = new MessageLike();
            messageLike.setUserId(comment.getUserId());
            messageLike.setId(comment.getId().toString());
            messageLike.setCreateDate(new DateTime(comment.getCreated()).toString("yyyy-MM-dd HH:mm"));
            messageLikeList.add(messageLike);
        }

        List<UserInfo> userInfoList = this.ssoService.findUserInfoList(userIdList);
        Map<Long,UserInfo> map = new HashMap<>();
        for (UserInfo userInfo : userInfoList) {
            map.put(userInfo.getUserId(),userInfo);
        }

        for (MessageLike messageLike : messageLikeList) {
            UserInfo userInfo = map.get(messageLike.getUserId());
            if (userInfo != null){
                messageLike.setAvatar(userInfo.getLogo());
                messageLike.setNickname(userInfo.getNickName());
            }
        }
        pageResult.setItems(messageLikeList);
        return pageResult;
    }

    @Autowired
    private AnnouncementService announcementService;
    public PageResult<Announcement> queryMessageAnnouncementList(int page, int pageSize) {

        IPage<Announcement> announcementIPage = announcementService.queryList(page, pageSize);

        List<MessageAnnouncement> list = new ArrayList<>();

        for (Announcement announcement : announcementIPage.getRecords()) {
            MessageAnnouncement messageAnnouncement = new MessageAnnouncement();
            messageAnnouncement.setId(String.valueOf(announcement.getId()));
            messageAnnouncement.setCreateDate(new DateTime(announcement.getCreated()).toString("yyyy-MM-dd HH:mm"));
            messageAnnouncement.setDescription(announcement.getDescription());
            messageAnnouncement.setTitle(announcement.getTitle());

            list.add(messageAnnouncement);
        }

        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);
        pageResult.setCounts(0);
        pageResult.setPages(0);
        pageResult.setItems(list);

        return pageResult;
    }
}
