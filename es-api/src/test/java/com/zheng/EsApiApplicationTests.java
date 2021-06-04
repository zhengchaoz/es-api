package com.zheng;

import com.alibaba.fastjson.JSON;
import com.zheng.pojo.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class EsApiApplicationTests {

    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    @Test
    void contextLoads() {
        System.out.println(client);
    }

    // 测试索引创建
    @Test
    public void testIndexCreation() throws IOException {
        // 创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("zheng_index");
        // 执行创建请求 IndicesClient
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        // 查看响应信息
        System.out.println(response);
    }

    // 测试获得索引
    @Test
    public void testGetIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("zheng_index");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // 测试删除索引
    @Test
    public void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("zheng_index");
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());// true 删除成功
    }

    // 测试创建文档
    @Test
    public void testAddDocument() throws IOException {
        User user = new User("郑超", 15);
        // 创建请求
        IndexRequest request = new IndexRequest("zheng_index");
        // 构建请求规则 put /zheng_index/_doc/1
        IndexRequest source = request.id("1").timeout(TimeValue.timeValueSeconds(1))
                .source(JSON.toJSONString(user), XContentType.JSON);
        // 发送请求
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        // 查看响应信息
        System.out.println(response.toString());// IndexResponse[index=zheng_index,type=_doc,id=1,version=1,result=created,seqNo=0,primaryTerm=1,shards={"total":2,"successful":1,"failed":0}]
        System.out.println(response.status());// CREATED 对应命令返回的状态
    }

    // 获得文档
    @Test
    public void testGetDocument() throws IOException {
        // 获得索引 get /zheng_index/_doc/1
        GetRequest getRequest = new GetRequest("zheng_index", "1");
        // 不获取返回的 _source 的上下文了
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        // 发送请求
        boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // 获得文档信息
    @Test
    public void testGetDocumentInfo() throws IOException {
        // 获得索引 get /zheng_index/_doc/1
        GetRequest getRequest = new GetRequest("zheng_index", "1");
        // 发送请求，响应数据
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(getResponse.getSourceAsString());// 返回数据
        System.out.println(getResponse);// 返回全部内容
    }

    // 更新文档信息
    @Test
    public void testUpdateDocumentInfo() throws IOException {
        UpdateRequest request = new UpdateRequest("zheng_index", "1");

        User user = new User("郑超", 500);
        request.timeout(TimeValue.timeValueSeconds(1)).doc(JSON.toJSONString(user), XContentType.JSON);

        UpdateResponse update = client.update(request, RequestOptions.DEFAULT);
        System.out.println(update.status());
    }

    // 删除文档信息
    @Test
    public void testDeleteDocumentInfo() throws IOException {
        DeleteRequest request = new DeleteRequest("zheng_index", "1");
        request.timeout("1s");
        DeleteResponse delete = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.status());
    }

    // 批量插入数据
    @Test
    public void testBulkRequest() throws IOException {
        BulkRequest request = new BulkRequest().timeout("10s");

        ArrayList<User> users = new ArrayList<>();
        users.add(new User("郑超", 15));
        users.add(new User("狗蛋", 11));
        users.add(new User("王麻子", 35));
        users.add(new User("师爷", 45));
        users.add(new User("李四", 66));

        for (int i = 0; i < users.size(); i++) {
            // 批量 更新、删除、获取 就是add()中的对象的不同
            // id() 可以省略，会生成随机id
            request.add(new IndexRequest("zheng_index")
                    .source(JSON.toJSONString(users.get(i)), XContentType.JSON));
//            request.add(new IndexRequest("zheng_index").id("" + (i + 1))
//                    .source(JSON.toJSONString(users.get(i)), XContentType.JSON));
        }

        BulkResponse bulk = client.bulk(request, RequestOptions.DEFAULT);
        System.out.println(bulk.hasFailures());// 是否成功，返回false代表没有错误，就是成功了
    }

    // 查询
    @Test
    public void testSearch() throws IOException {
        SearchRequest request = new SearchRequest("zheng_index");
        // 构建查询条件
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // 查询条件 matchAllQuery()匹配所有 termQuery() 精确匹配
        TermQueryBuilder termQuery = QueryBuilders.termQuery("name.keyword", "师爷");
        builder.query(termQuery).timeout(new TimeValue(60, TimeUnit.SECONDS));
        request.source(builder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(response.getHits()));

        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
        }
    }

}
