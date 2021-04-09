package com.itheima.tanhua.server.controller;

import com.itheima.tanhua.server.dto.RecommendUserQueryParam;
import com.itheima.tanhua.server.service.TodayBestService;
import com.itheima.tanhua.server.utils.Cache;
import com.itheima.tanhua.server.vo.NearUserVo;
import com.itheima.tanhua.server.vo.PageResult;
import com.itheima.tanhua.server.vo.TodayBest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("tanhua")
public class TodayBestController {
    @Autowired
    private TodayBestService todayBestService;


    @GetMapping("todayBest")
    @Cache(time = 60)
    public ResponseEntity<Object> todayBest(@RequestHeader("Authorization")String token){

        TodayBest todayBest = todayBestService.todayBest(token);

        if( todayBest != null){
            return ResponseEntity.ok(todayBest);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

/*
    @Cache(time = 60)
*/
    @GetMapping("recommendation")
    public ResponseEntity<Object> recommendation(@RequestHeader("Authorization")String token, RecommendUserQueryParam recommendUserQueryParam){
        PageResult recommendation = todayBestService.recommendation(token, recommendUserQueryParam);
        if (recommendation != null){
            return ResponseEntity.ok(recommendation);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    //佳人信息
    // GET/tanhua/:id/personalInfo

    @GetMapping("{id}/personalInfo")
    public ResponseEntity<Object> personalInfo(@PathVariable("id") Long userId){
        //todayBest
        TodayBest todayBest = this.todayBestService.personalInfo(userId);

        if (todayBest != null){
            return ResponseEntity.ok(todayBest);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("strangerQuestions")
    public ResponseEntity<Object> strangerQuestions(@RequestParam("userId") Long userId){


        try {

            String question = todayBestService.queryQuestion(userId);
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    // TODO: 2021/3/14 回复陌生人消息未做

    @PostMapping("strangerQuestions")
    public ResponseEntity<Object> reply(@RequestBody Map<String, Object> param){
        Long userId = Long.parseLong(param.get("userId").toString());
        String reply = String.valueOf(param.get("reply"));

            boolean isSuccess = todayBestService.reply(userId,reply);
            if (isSuccess){
                return ResponseEntity.ok(null);
            }


        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 搜附近
     *
     * @param gender
     * @param distance
     * @return
     */
    @GetMapping("search")
    public ResponseEntity<List<NearUserVo>> queryNearUser(@RequestParam(value = "gender", required = false) String gender,
                                                          @RequestParam(value = "distance", defaultValue = "2000") Long distance) {
        try {
            List<NearUserVo> list = this.todayBestService.nearBy(gender, distance);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    @GetMapping("cards")
    public ResponseEntity<Object> cards(){
        try {
            List<TodayBest> list = todayBestService.cards();

            return ResponseEntity.ok(list);
        }catch (Exception e){
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("{id}/love")
    public ResponseEntity<Object> love(@PathVariable("id") Long likeUserId){
        try {

            todayBestService.love(likeUserId);
            return ResponseEntity.ok(null);
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


    }

    @GetMapping("{id}/unlove")
    public ResponseEntity<Void> unlove(@PathVariable("id") Long likeUserId) {
        try {
            this.todayBestService.removeLikeUser(likeUserId);
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }



}
