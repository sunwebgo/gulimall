package com.xha.gulimall.search.service.impl;

import com.xha.gulimall.search.constants.EsConstants;
import com.xha.gulimall.search.dto.ParamDTO;
import com.xha.gulimall.search.dto.ResponseDTO;
import com.xha.gulimall.search.service.MallSearchService;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Objects;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public ResponseDTO searchProducts(ParamDTO paramDTO) {
//        1.创建SearchRequest对象
        SearchRequest searchRequest = new SearchRequest();
//        2.创建检索请求
        buildSearchRequest(paramDTO);
        try {
//        2.执行检索
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 创建检索请求
     *
     * @param paramDTO param dto
     * @return {@link SearchRequest}
     */
    private SearchRequest buildSearchRequest(ParamDTO paramDTO) {
//        1.构建DSL语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

//        2.模糊匹配、过滤（按照属性、分类、品牌、价格区间、库存）
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//          2.1查询检索关键字
        if (!StringUtils.isEmpty(paramDTO.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", paramDTO.getKeyword()));
        }
//          2.2查询三级分类id
        if (!Objects.isNull(paramDTO.getCatalog3Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catelogId", paramDTO.getCatalog3Id()));
        }
//          2.3查询品牌id
        if (CollectionUtils.isEmpty(paramDTO.getBrandId())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandId", paramDTO.getBrandId()));
        }
//          2.4是否有库存
        boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", paramDTO.getHasStock() == 1));

//          2.5按照价格区间查询
        if (!StringUtils.isEmpty(paramDTO.getSkuPrice())) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            String[] prices = paramDTO.getSkuPrice().split("_");
            if (prices.length == 2) {
                rangeQueryBuilder.gte(prices[0]);
                rangeQueryBuilder.lte(prices[1]);
            } else if (prices.length == 1) {
                if (paramDTO.getSkuPrice().startsWith("_")) {
                    rangeQueryBuilder.lte(prices[0]);
                } else {
                    rangeQueryBuilder.gte(prices[0]);
                }

            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

//          2.6按照属性进行查询
        if (!CollectionUtils.isEmpty(paramDTO.getAttrs())) {
            for (String attr : paramDTO.getAttrs()) {
                BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
//                attrs=1_5寸:8寸&attrs2_8G:16G
                String[] attrArray = attr.split("_");
                String attrId = attrArray[0];
                String[] attrValues = attrArray[1].split(":");
                queryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                queryBuilder.must(QueryBuilders.termQuery("attrs.attrValue", attrValues));
//                2.6.1将nestQuery放入到循环当中是为了每次循环都创建一个NestedQueryBuilder
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", queryBuilder, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            }
        }


        searchSourceBuilder.query(boolQueryBuilder);

//        3.排序、分页、高亮
//          3.1排序
        if (!StringUtils.isEmpty(paramDTO.getSort())) {
            String sort = paramDTO.getSort();
//            sort=hotScore_asc/desc
            String[] sortSplit = sort.split("_");
            SortOrder sortOrder = sortSplit[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(sortSplit[0], sortOrder);
        }
//          3.2分页
        searchSourceBuilder.from((paramDTO.getPageNum() - 1) * EsConstants.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstants.PRODUCT_PAGESIZE);

//          3.3高亮
        if (!StringUtils.isEmpty(paramDTO.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder
                    .field("skuTitle")
                    .preTags("<b style='color:red'>")
                    .postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

//        4.聚合查询

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstants.PRODUCT_INDEX}, searchSourceBuilder);
        return searchRequest;
    }
}
