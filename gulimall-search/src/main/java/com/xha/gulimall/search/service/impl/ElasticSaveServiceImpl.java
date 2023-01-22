package com.xha.gulimall.search.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xha.gulimall.common.to.es.SkuInfoES;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.search.config.ElasticsearchConfig;
import com.xha.gulimall.search.constants.EsConstants;
import com.xha.gulimall.search.service.ElasticSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ElasticSaveServiceImpl implements ElasticSaveService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 上传sku信息
     *
     * @param upProducts 了产品
     * @return {@link R}
     */
    @Override
    public boolean upProduct(List<SkuInfoES> upProducts) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuInfoES product : upProducts) {
            IndexRequest indexRequest = new IndexRequest();
//            指定索引
            indexRequest.index(EsConstants.PRODUCT_INDEX);
//            指定id
            indexRequest.id(product.getSkuId().toString());
//            将上传的对象转换为JSON数据
            String upProductsString = new ObjectMapper().writeValueAsString(product);
//            指定source，并且为JSON类型
            indexRequest.source(upProductsString, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ElasticsearchConfig.COMMON_OPTIONS);

        boolean result = bulk.hasFailures();
        return result;
    }
}
