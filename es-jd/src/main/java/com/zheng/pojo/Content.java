package com.zheng.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author 郑超
 * @create 2021/6/5
 */
@Data
@Component
@AllArgsConstructor
@NoArgsConstructor
public class Content {

    private String img;
    private String price;
    private String title;
}
