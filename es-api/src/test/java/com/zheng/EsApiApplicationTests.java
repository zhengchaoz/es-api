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

    // ??????????????????
    @Test
    public void testIndexCreation() throws IOException {
        // ??????????????????
        CreateIndexRequest request = new CreateIndexRequest("zheng_index");
        // ?????????????????? IndicesClient
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        // ??????????????????
        System.out.println(response);
    }

    // ??????????????????
    @Test
    public void testGetIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("zheng_index");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // ??????????????????
    @Test
    public void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("zheng_index");
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());// true ????????????
    }

    // ??????????????????
    @Test
    public void testAddDocument() throws IOException {
        User user = new User("??????", 15);
        // ????????????
        IndexRequest request = new IndexRequest("zheng_index");
        // ?????????????????? put /zheng_index/_doc/1
        IndexRequest source = request.id("1").timeout(TimeValue.timeValueSeconds(1))
                .source(JSON.toJSONString(user), XContentType.JSON);
        // ????????????
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        // ??????????????????
        System.out.println(response.toString());// IndexResponse[index=zheng_index,type=_doc,id=1,version=1,result=created,seqNo=0,primaryTerm=1,shards={"total":2,"successful":1,"failed":0}]
        System.out.println(response.status());// CREATED ???????????????????????????
    }

    // ????????????
    @Test
    public void testGetDocument() throws IOException {
        // ???????????? get /zheng_index/_doc/1
        GetRequest getRequest = new GetRequest("zheng_index", "1");
        // ?????????????????? _source ???????????????
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        // ????????????
        boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // ??????????????????
    @Test
    public void testGetDocumentInfo() throws IOException {
        // ???????????? get /zheng_index/_doc/1
        GetRequest getRequest = new GetRequest("zheng_index", "1");
        // ???????????????????????????
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(getResponse.getSourceAsString());// ????????????
        System.out.println(getResponse);// ??????????????????
    }

    // ??????????????????
    @Test
    public void testUpdateDocumentInfo() throws IOException {
        UpdateRequest request = new UpdateRequest("zheng_index", "1");

        User user = new User("??????", 500);
        request.timeout(TimeValue.timeValueSeconds(1)).doc(JSON.toJSONString(user), XContentType.JSON);

        UpdateResponse update = client.update(request, RequestOptions.DEFAULT);
        System.out.println(update.status());
    }

    // ??????????????????
    @Test
    public void testDeleteDocumentInfo() throws IOException {
        DeleteRequest request = new DeleteRequest("zheng_index", "1");
        request.timeout("1s");
        DeleteResponse delete = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.status());
    }

    // ??????????????????
    @Test
    public void testBulkRequest() throws IOException {
        BulkRequest request = new BulkRequest().timeout("10s");

        ArrayList<User> users = new ArrayList<>();
        users.add(new User("??????", 15));
        users.add(new User("??????", 11));
        users.add(new User("?????????", 35));
        users.add(new User("??????", 45));
        users.add(new User("??????", 66));

        for (int i = 0; i < users.size(); i++) {
            // ?????? ???????????????????????? ??????add()?????????????????????
            // id() ??????????????????????????????id
            request.add(new IndexRequest("zheng_index")
                    .source(JSON.toJSONString(users.get(i)), XContentType.JSON));
//            request.add(new IndexRequest("zheng_index").id("" + (i + 1))
//                    .source(JSON.toJSONString(users.get(i)), XContentType.JSON));
        }

        BulkResponse bulk = client.bulk(request, RequestOptions.DEFAULT);
        System.out.println(bulk.hasFailures());// ?????????????????????false????????????????????????????????????
    }

    // ??????
    @Test
    public void testSearch() throws IOException {
        SearchRequest request = new SearchRequest("zheng_index");
        // ??????????????????
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // ???????????? matchAllQuery()???????????? termQuery() ????????????
        TermQueryBuilder termQuery = QueryBuilders.termQuery("name.keyword", "??????");
        builder.query(termQuery).timeout(new TimeValue(60, TimeUnit.SECONDS));
        request.source(builder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(response.getHits()));

        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
        }
    }

}
