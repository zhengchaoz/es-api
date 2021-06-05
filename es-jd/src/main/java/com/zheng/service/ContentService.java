package com.zheng.service;

import com.alibaba.fastjson.JSON;
import com.zheng.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 郑超
 * @create 2021/6/5
 */
@Service
public class ContentService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private HtmlParseUtil htmlParseUtil;

    public Boolean parseContent(String keywords) throws IOException {
        List<Object> contents = htmlParseUtil.parseJD(keywords);

        BulkRequest bulkRequest = new BulkRequest().timeout("2m");
        for (Object content : contents)
            bulkRequest.add(new IndexRequest("jd_goods")
                    .source(JSON.toJSONString(content), XContentType.JSON));

        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }

    public List<Map<String, Object>> searchPage(String keywords, int pageNo, int pageSize) throws IOException {
        if (pageNo < 1) pageNo = 1;
        // 精准查询
        TermQueryBuilder query = QueryBuilders.termQuery("title", keywords);
        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("title")
                .preTags("<span style='color:red'>").postTags("</span>")
                .requireFieldMatch(false);// 是否高亮多个
        // 构建查询条件，分页
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.from(pageNo).size(pageSize).query(query).timeout(new TimeValue(60, TimeUnit.SECONDS))
                .highlighter(highlightBuilder);
        // 执行搜索
        SearchRequest request = new SearchRequest("jd_goods").source(builder);
        SearchResponse search = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        // 解析结果
        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit hit : search.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 获取高亮字段，实际项目中这段逻辑最好放在前端用js实现
            HighlightField title = hit.getHighlightFields().get("title");
            if (title != null) {
                StringBuilder str = new StringBuilder();
                for (Text text : title.fragments()) str.append(text);
                sourceAsMap.put("title", str.toString());
            }

            list.add(sourceAsMap);
        }
        return list;
    }
}
