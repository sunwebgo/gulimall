package com.xha.gulimall.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xha.gulimall.search.config.ElasticsearchConfig;
import com.xha.gulimall.search.constants.EsConstants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;

@Slf4j
@SpringBootTest
class GulimallSearchApplicationTests {

    @Resource
    private RestHighLevelClient rhlc;


    @Data
    class User {
        private String username;
        private String gender;
        private Integer age;
    }

    @Data
    static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }


    /**
     * 保存数据到es
     */
    @Test
    public void saveData() throws IOException {
//        指定索引
        IndexRequest indexRequest = new IndexRequest("users");
//        数据id
        indexRequest.id("1");
        User user = new User();
        user.setUsername("zhangsan");
        user.setGender("男");
        user.setAge(20);
        ObjectMapper mapper = new ObjectMapper();
//        将User对象转换为JSON数据
        String userJsonData = mapper.writeValueAsString(user);
//        要保存的内容
        indexRequest.source(userJsonData, XContentType.JSON);

//        执行操作
        IndexResponse response = rhlc.index(indexRequest, ElasticsearchConfig.COMMON_OPTIONS);

        System.out.println(response);
    }

    @Test
    public void testJSON() throws JsonProcessingException {
        User user = new User();
        user.setUsername("zhangsan");
        user.setGender("男");
        user.setAge(20);
        ObjectMapper mapper = new ObjectMapper();
        String userJSON = mapper.writeValueAsString(user);
        System.out.println(userJSON);
    }

    @Test
    public void searchData() throws IOException {
//        1.构建检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        2.检索address字段中有mill的数据
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
//        3.按照年龄的值分布进行聚合
        TermsAggregationBuilder ageGroup = AggregationBuilders.terms("ageGroup").field("age").size(10);
//        4.聚合计算平均薪资
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");

        searchSourceBuilder
                .aggregation(ageGroup)
                .aggregation(balanceAvg);


//        5.指定索引和检索条件
//          5.1创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        searchRequest
                .indices("bank")
                .source(searchSourceBuilder);

//        6.执行检索
        SearchResponse responseSearch = rhlc.search(searchRequest, RequestOptions.DEFAULT);

//        7.处理聚合结果
        Aggregations aggregations = responseSearch.getAggregations();
        Terms ageGroups = aggregations.get("ageGroup");
        for (Terms.Bucket bucket : ageGroups.getBuckets()) {
            String key = bucket.getKeyAsString();
            log.info("年龄：" + key + "===>" + "一共" + bucket.getDocCount() + "人。");
        }

        Avg balanceAvgs = aggregations.get("balanceAvg");
        log.info("平均薪资：" + balanceAvgs.getValue());
    }

    @Test
    public void test1(){
        System.out.println("--------------");
        int i = (int) Math.ceil(((double) 1 / EsConstants.PRODUCT_PAGESIZE));
        System.out.println(i);
        System.out.println("--------------");

    }

}
