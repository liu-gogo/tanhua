package com.itheima.tanhua.dubbo.server.api;

import com.itheima.tanhua.dubbo.server.pojo.Video;
import com.itheima.tanhua.dubbo.server.vo.PageInfo;

import java.util.List;

public interface VideoApi {
    /**
     * 保存小视频
     *
     * @param video
     * @return
     */
    Boolean saveVideo(Video video);

    List<Video> queryVideoList(Integer pageNum,Integer pageSize);

    /**
     * 关注用户
     * @param userId
     * @param followUserId
     * @return
     */
    boolean saveFollowUser(Long userId,Long followUserId);

    /**
     * 取消关注
     * @param userId
     * @param followUserId
     * @return
     */
    boolean removeFollowUser(Long userId,Long followUserId);
}
