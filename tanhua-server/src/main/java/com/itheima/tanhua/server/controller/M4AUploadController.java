package com.itheima.tanhua.server.controller;

import com.itheima.tanhua.server.service.M4AUploadService;
import com.itheima.tanhua.server.vo.PicUploadResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("peachblossom")
public class M4AUploadController {

    @Autowired
    private M4AUploadService m4AUploadService;

    public ResponseEntity<Object> ma4(@RequestParam("soundFile")MultipartFile multipartFile){
        PicUploadResult upload = m4AUploadService.upload(multipartFile);
        return ResponseEntity.ok(upload);
    }
}
