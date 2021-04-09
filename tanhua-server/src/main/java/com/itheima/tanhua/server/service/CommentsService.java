package com.itheima.tanhua.server.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.tanhua.dubbo.server.api.QuanZiApi;
import com.itheima.tanhua.dubbo.server.pojo.Comment;
import com.itheima.tanhua.dubbo.server.vo.PageInfo;
import com.itheima.tanhua.server.utils.UserThreadLocal;
import com.itheima.tanhua.server.vo.Comments;
import com.itheima.tanhua.server.vo.PageResult;
import com.itheima.tanhua.sso.pojo.User;
import com.itheima.tanhua.sso.pojo.UserInfo;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentsService {
    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Autowired
    private SSOService ssoService;




    /**
     * 单条动态的评论列表
     * @param publishId
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult comments(String publishId, int page, int pageSize) {
        User user = UserThreadLocal.get();
        PageInfo<Comment> commentPageInfo = quanZiApi.queryCommentList(publishId, page, pageSize);
        List<Comment> commentList = commentPageInfo.getList();


        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);
        pageResult.setPages(0);
        pageResult.setCounts(0);

        if (CollectionUtils.isEmpty(commentList)){
            return pageResult;
        }

        List<Comments> commentsList = new ArrayList<>();

        List<Long> userIdList = new ArrayList<>();

        for (Comment comment : commentList) {
            if (!userIdList.contains(comment.getUserId())){
                userIdList.add(comment.getUserId());
            }
            Comments comments = new Comments();
            comments.setUserId(comment.getUserId());
            comments.setContent(comment.getContent());
            comments.setCreateDate(new DateTime(comment.getCreated()).toString("yyyy年MM月dd日 HH:mm"));
            comments.setId(comment.getId().toHexString());

            commentsList.add(comments);
        }

        List<UserInfo> userInfoList = this.ssoService.findUserInfoList(userIdList);
        Map<Long,UserInfo> map = new HashMap<>();
        for (UserInfo userInfo : userInfoList) {
            map.put(userInfo.getUserId(),userInfo);
        }
        for (Comments comments : commentsList) {
            UserInfo userInfo = map.get(comments.getUserId());
            if (userInfo != null){
                comments.setAvatar(userInfo.getLogo());
                comments.setNickname(userInfo.getNickName());
            }
            comments.setLikeCount(0);// TODO
            comments.setHasLiked(0);// TODO
        }
        pageResult.setCounts((int) commentPageInfo.getCount());
        pageResult.setItems(commentsList);
        return pageResult;

    }

    public boolean saveContent(String publishId, String comment) {
        User user = UserThreadLocal.get();

        return  quanZiApi.saveComment(user.getId(), publishId, comment, 2);
    }
}
