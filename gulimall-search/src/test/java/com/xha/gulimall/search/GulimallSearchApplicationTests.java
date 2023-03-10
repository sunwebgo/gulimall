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
     * ???????????????es
     */
    @Test
    public void saveData() throws IOException {
//        ????????????
        IndexRequest indexRequest = new IndexRequest("users");
//        ??????id
        indexRequest.id("1");
        User user = new User();
        user.setUsername("zhangsan");
        user.setGender("???");
        user.setAge(20);
        ObjectMapper mapper = new ObjectMapper();
//        ???User???????????????JSON??????
        String userJsonData = mapper.writeValueAsString(user);
//        ??????????????????
        indexRequest.source(userJsonData, XContentType.JSON);

//        ????????????
        IndexResponse response = rhlc.index(indexRequest, ElasticsearchConfig.COMMON_OPTIONS);

        System.out.println(response);
    }

    @Test
    public void testJSON() throws JsonProcessingException {
        User user = new User();
        user.setUsername("zhangsan");
        user.setGender("???");
        user.setAge(20);
        ObjectMapper mapper = new ObjectMapper();
        String userJSON = mapper.writeValueAsString(user);
        System.out.println(userJSON);
    }

    @Test
    public void searchData() throws IOException {
//        1.??????????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        2.??????address????????????mill?????????
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
//        3.????????????????????????????????????
        TermsAggregationBuilder ageGroup = AggregationBuilders.terms("ageGroup").field("age").size(10);
//        4.????????????????????????
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");

        searchSourceBuilder
                .aggregation(ageGroup)
                .aggregation(balanceAvg);


//        5.???????????????????????????
//          5.1??????????????????
        SearchRequest searchRequest = new SearchRequest();
        searchRequest
                .indices("bank")
                .source(searchSourceBuilder);

//        6.????????????
        SearchResponse responseSearch = rhlc.search(searchRequest, RequestOptions.DEFAULT);

//        7.??????????????????
        Aggregations aggregations = responseSearch.getAggregations();
        Terms ageGroups = aggregations.get("ageGroup");
        for (Terms.Bucket bucket : ageGroups.getBuckets()) {
            String key = bucket.getKeyAsString();
            log.info("?????????" + key + "===>" + "??????" + bucket.getDocCount() + "??????");
        }

        Avg balanceAvgs = aggregations.get("balanceAvg");
        log.info("???????????????" + balanceAvgs.getValue());
    }

    @Test
    public void test1(){


    }

}
