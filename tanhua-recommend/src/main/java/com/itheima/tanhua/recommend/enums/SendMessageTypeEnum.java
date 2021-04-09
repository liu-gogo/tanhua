package com.itheima.tanhua.recommend.enums;

public enum SendMessageTypeEnum {
    //1-发动态，2-浏览动态， 3-点赞， 4-喜欢， 5-评论，6-取消点赞，7-取消喜欢

    PUBLISH(1,"发动态"),
    WATCH_PUBLISH(2,"浏览动态"),
    LIKE(3,"点赞"),
    LOVE(4,"喜欢"),
    COMMENT(5,"评论"),
    CANCEL_LIKE(6,"取消点赞"),
    CANCEL_LOVE(7,"取消喜欢");

    private int value;
    private String desc;

    SendMessageTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return this.desc;
    }

    public int getValue() {
        return value;
    }
}
