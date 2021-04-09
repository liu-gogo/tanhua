package com.itheima.tanhua.server.vo;

import lombok.Data;

@Data
public class PicUploadResult {

    private boolean isSuccess;

    private String imageUrl;

    private String imagePath;
}
