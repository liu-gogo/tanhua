package com.itheima.tanhua.sso.vo;

import lombok.Data;

@Data
public class PicUploadResult {

    private boolean isSuccess;

    private String imageUrl;

    private String imagePath;
}
