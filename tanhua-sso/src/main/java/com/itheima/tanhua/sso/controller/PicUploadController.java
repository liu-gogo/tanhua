package com.itheima.tanhua.sso.controller;

import com.itheima.tanhua.sso.service.PicUploadService;
import com.itheima.tanhua.sso.vo.PicUploadResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("pic")
public class PicUploadController {
    @Autowired
    private PicUploadService picUploadService;

    @RequestMapping("upload")
    public ResponseEntity<Object> upload(@RequestParam("file") MultipartFile multipartFile){
        PicUploadResult upload = picUploadService.upload(multipartFile);
        return ResponseEntity.ok(upload);
    }
}
