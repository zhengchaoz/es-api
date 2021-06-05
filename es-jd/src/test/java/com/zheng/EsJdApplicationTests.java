package com.zheng;

import com.zheng.service.ContentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class EsJdApplicationTests {

    @Autowired
    private ContentService contentService;

    @Test
    void contextLoads() throws IOException {
        System.out.println(contentService.parseContent("java"));
    }

}
