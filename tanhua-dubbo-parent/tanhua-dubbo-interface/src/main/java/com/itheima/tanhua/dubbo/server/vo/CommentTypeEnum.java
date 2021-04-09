package com.itheima.tanhua.dubbo.server.vo;

public enum CommentTypeEnum {

    LIKE(1,"点赞"),
    COMMENT(2,"评论"),
    LOVE(3,"喜欢");

    private int code;
    private String message;
    CommentTypeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
