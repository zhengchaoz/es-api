package com.zheng.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 构建Elasticsearch的bean对象
 *
 * @author 郑超
 * @create 2021/6/4
 */
@Configuration
public class ElasticsearchClientConfig {

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(
                RestClient.builder(
//                        new HttpHost("localhost", 9201, "http"), 如果是集群就构建多个
                        new HttpHost("localhost", 9200, "http")
                ));
    }
}
