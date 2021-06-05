package com.zheng.controller;

import com.zheng.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author 郑超
 * @create 2021/6/5
 */
@RestController
public class ContentController {

    @Autowired
    private ContentService contentService;

    @GetMapping("/parse/{keywords}")
    public Boolean parse(@PathVariable String keywords) throws IOException {
        return contentService.parseContent(keywords);
    }

    @GetMapping("/search/{keywords}/{pageNo}/{pageSize}")
    public List<Map<String, Object>> searchPage(
            @PathVariable String keywords, @PathVariable int pageNo, @PathVariable int pageSize) throws IOException {
        return contentService.searchPage(keywords, pageNo, pageSize);
    }
}
