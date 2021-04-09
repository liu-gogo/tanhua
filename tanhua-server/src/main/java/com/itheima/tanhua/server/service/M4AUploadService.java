package com.itheima.tanhua.server.service;

import com.aliyun.oss.OSSClient;
import com.itheima.tanhua.server.config.AliyunConfig;
import com.itheima.tanhua.server.vo.PicUploadResult;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class M4AUploadService {
    @Autowired
    private AliyunConfig aliyunConfig;

    @Autowired
    private OSSClient ossClient;

    private static final String[] M4A_TYPE = new String[]{".m4a"};

    public PicUploadResult upload(MultipartFile multipartFile) {
        PicUploadResult picUploadResult = new PicUploadResult();

        boolean isLegal = false;
        String fileName = multipartFile.getOriginalFilename();
        for (String type : M4A_TYPE) {
            if(StringUtils.endsWithIgnoreCase(fileName,type)){
                isLegal = true;
                break;

            }
        }

        if(!isLegal){
            picUploadResult.setSuccess(false);
            return picUploadResult;
        }

        String name = getM4AFileName(fileName);

        try {
            ossClient.putObject(aliyunConfig.getBucketName(), fileName, new ByteArrayInputStream(multipartFile.getBytes()));
            picUploadResult.setSuccess(true);
            picUploadResult.setImagePath(fileName);
            picUploadResult.setImageUrl(aliyunConfig.getUrlPrefix() + fileName);
            return picUploadResult;
        } catch (IOException e) {

            e.printStackTrace();
        }

        picUploadResult.setSuccess(false);
        return picUploadResult;

    }


    private String getM4AFileName(String fileName) {
        DateTime dateTime = new DateTime();
        String year = dateTime.toString("yyyy");
        String month = dateTime.toString("MM");
        String day = dateTime.toString("dd");

        long filename = System.currentTimeMillis() + RandomUtils.nextInt(1000, 9999);

        return "m4a/" + year + "/" + month + "/" + day + "/" +fileName + "." + StringUtils.endsWithIgnoreCase(fileName, ".");

    }
}
