package com.itheima.tanhua.sso.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class FaceEngineServiceTest {
    @Autowired
    private FaceEngineService faceEngineService;

    @Test
    public void testFaceEngine(){
        File file = new File("C:\\Users\\liu\\Desktop\\50.png");
        boolean b = faceEngineService.checkIsPortrait(file);
        System.out.println(b);
    }


}