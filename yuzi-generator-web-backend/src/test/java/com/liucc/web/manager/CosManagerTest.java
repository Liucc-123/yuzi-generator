package com.liucc.web.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CosManagerTest {

    @Resource
    private CosManager cosManager;

    @Test
    void deletebject() {
        cosManager.deletebject("/WechatIMG32.jpg");
    }

    @Test
    void deleteObjects() {
        cosManager.deleteObjects(Arrays.asList("generator_picture/1916758884995940353/QsfaihHC-WechatIMG32.jpg",
                "generator_picture/1916758884995940353/cyNYNwlz-WechatIMG32.jpg "));
    }

    @Test
    void deleteDir() {
        cosManager.deleteDir("/test/");
    }
}