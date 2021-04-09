package com.itheima.tanhua.dubbo.server.api;

import com.itheima.tanhua.dubbo.server.pojo.Comment;
import com.itheima.tanhua.dubbo.server.pojo.Publish;
import com.itheima.tanhua.dubbo.server.vo.PageInfo;

import java.util.List;

public interface QuanZiApi {
    /**
     * 发布动态
     * @param userId
     * @param publish
     * @return
     */
    String savePublish(Long userId,Publish publish);


    List<Publish> queryPublishList(Long userId, Integer pageNum, Integer pageSize,boolean isRecommend);


    /**
     * 保存评论（点赞，评论，喜欢）
     * @param userId
     * @param publishId
     * @param content
     * @param commentType
     * @return
     */
    boolean saveComment(Long userId,String publishId,String content,int commentType);

    /**
     * 取消点赞以及喜欢
     * @param userId
     * @param publishId
     * @param commentType
     * @return
     */
    boolean removeComment(Long userId,String publishId,int commentType);

    /**
     * 点赞，喜欢,评论的数量
     * @param publishId
     * @param commentType
     * @return
     */
    long queryCommentCount(String publishId,int commentType);

    /**
     * 查询单条动态
     * @param publishId
     * @return
     */
    Publish queryPublish(String publishId);

    /**
     * 查询评论
     *
     * @return
     */
    PageInfo<Comment> queryCommentList(String publishId, Integer page, Integer pageSize);


    List<Comment> queryCommentListByPublishUserId(Long userId,int commentType);


    List<Publish> queryPublishByPids(List<Long> pidList);

    List<Publish> queryAlbum(Long userId, int page, int pageSize);
}
