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

@Service
public class PicUploadService {

    // 允许上传的格式
    private static final String[] IMAGE_TYPE = new String[]{".bmp", ".jpg",
            ".jpeg", ".gif", ".png"};

    @Autowired
    private OSSClient ossClient;

    @Autowired
    private AliyunConfig aliyunConfig;

    public PicUploadResult upload(MultipartFile multipartFile){
        /**
         * 1. 判断图片是否符合需求 （上传的文件是不是图片，根据后缀来进行判断）
         * 2. 执行上传，为了防止文件名重复，以及便于管理，设置文件的路径，以及文件名（唯一）
         * 3. 调用阿里云oss上传，如果上传成功，返回结果
         */
        //aaa.jpg
        String originalFilename = multipartFile.getOriginalFilename();
        boolean isLegal = false;
        for (String suffix : IMAGE_TYPE) {
            if (originalFilename.endsWith(suffix)){
                //图片合法
                isLegal = true;
                break;
            }
        }
        PicUploadResult picUploadResult = new PicUploadResult();
        if (!isLegal){
            picUploadResult.setSuccess(false);
            return picUploadResult;
        }
        String fileName = genFileName(originalFilename);


        try {
            //上传 不抛异常 就是成功
            //String bucketName, String key, InputStream input
            //空间名称 oss上传自己建的，key 文件的名称（images/xxx/xxx/xx.jpg）,文件流，输入流
            ossClient.putObject(aliyunConfig.getBucketName(), fileName, new ByteArrayInputStream(multipartFile.getBytes()));
            picUploadResult.setSuccess(true);
            picUploadResult.setImagePath(fileName);
            picUploadResult.setImageUrl(aliyunConfig.getUrlPrefix()+fileName);

            return picUploadResult;
        } catch (Exception e) {
            e.printStackTrace();
        }

        picUploadResult.setSuccess(false);

        return picUploadResult;
    }

    private String genFileName(String originalFilename) {
        //images/yyyy/MM/dd/a123123123.jpg
        DateTime dateTime = new DateTime();
        String year = dateTime.toString("yyyy");
        String month = dateTime.toString("MM");
        String day = dateTime.toString("dd");
        long fileName = System.currentTimeMillis() + RandomUtils.nextInt(1000, 9999);
        //aaa.jpg
        return "images/"+year+"/"+month+"/"+day+"/"+fileName+"."+ StringUtils.substringAfterLast(originalFilename,".");
    }
}
