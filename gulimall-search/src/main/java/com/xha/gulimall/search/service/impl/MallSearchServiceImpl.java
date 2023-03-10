package com.xha.gulimall.search.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xha.gulimall.common.to.es.SkuInfoES;
import com.xha.gulimall.search.config.ElasticsearchConfig;
import com.xha.gulimall.search.constants.EsConstants;
import com.xha.gulimall.search.dto.ParamDTO;
import com.xha.gulimall.search.service.MallSearchService;
import com.xha.gulimall.search.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResponseVO searchProducts(ParamDTO paramDTO) {
//        1.??????????????????
        SearchRequest searchRequest = buildSearchRequest(paramDTO);
        SearchResponseVO searchResponseVO = null;
        try {
//        2.????????????
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, ElasticsearchConfig.COMMON_OPTIONS);
//        3.????????????????????????
            searchResponseVO = handlerResponse(searchResponse, paramDTO);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResponseVO;
    }


    /**
     * ??????????????????
     *
     * @param paramDTO param dto
     * @return {@link SearchRequest}
     */
    private SearchRequest buildSearchRequest(ParamDTO paramDTO) {
//        1.??????DSL??????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

//        2.?????????????????????????????????????????????????????????????????????????????????
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//          2.1?????????????????????
        if (!StringUtils.isEmpty(paramDTO.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", paramDTO.getKeyword()));
        }
//          2.2??????????????????id
        if (!Objects.isNull(paramDTO.getCatalog3Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catelogId", paramDTO.getCatalog3Id()));
        }
//          2.3????????????id
        if (!CollectionUtils.isEmpty(paramDTO.getBrandId())) {
            for (Long brandId : paramDTO.getBrandId()) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("brandId", brandId));
            }
        }
//          2.4???????????????,?????????????????????
        if (!Objects.isNull(paramDTO.getHasStock())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", paramDTO.getHasStock() == 1));
        }

//          2.5????????????????????????
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

//          2.6????????????????????????
        if (!CollectionUtils.isEmpty(paramDTO.getAttrs())) {
            for (String attr : paramDTO.getAttrs()) {
                BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
//                attrs=1_5???:8???&attrs2_8G:16G
                String[] attrArray = attr.split("_");
                String attrId = attrArray[0];
                String[] attrValues = attrArray[1].split(":");
                queryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                queryBuilder.must(QueryBuilders.termQuery("attrs.attrValue", attrValues));
//                2.6.1???nestQuery?????????????????????????????????????????????????????????NestedQueryBuilder
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", queryBuilder, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            }
        }


        searchSourceBuilder.query(boolQueryBuilder);

//        3.????????????????????????
//          3.1??????
        if (!StringUtils.isEmpty(paramDTO.getSort())) {
            String sort = paramDTO.getSort();
//            sort=hotScore_asc/desc
            String[] sortSplit = sort.split("_");
            SortOrder sortOrder = sortSplit[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(sortSplit[0], sortOrder);
        }
//          3.2??????
        searchSourceBuilder.from((paramDTO.getPageNum() - 1) * EsConstants.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstants.PRODUCT_PAGESIZE);

//          3.3??????
        if (!StringUtils.isEmpty(paramDTO.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder
                    .field("skuTitle")
                    .preTags("<b style='color:red'>")
                    .postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

//        4.????????????
//          4.1????????????
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
//          4.2????????????????????????
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brand_agg);

//          4.3????????????
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catelogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catelogName.keyword").size(1));
        searchSourceBuilder.aggregation(catalog_agg);

//          4.4????????????
//              4.4.1??????????????????????????????attr??????
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
//              4.4.2???????????????attr_id???????????????
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);

        searchSourceBuilder.aggregation(attr_agg);


        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstants.PRODUCT_INDEX}, searchSourceBuilder);
        return searchRequest;
    }


    /**
     * ??????????????????
     *
     * @param searchResponse ????????????
     */
    private SearchResponseVO handlerResponse(SearchResponse searchResponse, ParamDTO paramDTO) {
        SearchResponseVO searchResponseVO = new SearchResponseVO();

        SearchHits hits = searchResponse.getHits();
//        1.??????????????????
        searchResponseVO.setTotal(hits.getTotalHits().value);
//        2.???????????????
        searchResponseVO.setTotalPages((int) Math.ceil(((double) hits.getTotalHits().value) / EsConstants.PRODUCT_PAGESIZE));
//        3.??????????????????
        searchResponseVO.setPageNum(paramDTO.getPageNum());
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= (int) Math.ceil(((double) hits.getTotalHits().value) / EsConstants.PRODUCT_PAGESIZE); i++) {
            pageNavs.add(i);
        }

//        4.????????????????????????????????????
        List<SkuInfoES> skuInfoESList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(Arrays.asList(hits.getHits()))) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                ObjectMapper mapper = new ObjectMapper();
                SkuInfoES skuInfoES = null;
                try {
                    skuInfoES = mapper.readValue(sourceAsString, SkuInfoES.class);
//                    4.1????????????
                    if (!StringUtils.isEmpty(paramDTO.getKeyword())) {
                        HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                        skuInfoES.setSkuTitle(skuTitle.getFragments()[0].string());
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                skuInfoESList.add(skuInfoES);
            }
        }
        searchResponseVO.setProducts(skuInfoESList);

//        5.???????????????????????????????????????
        List<CategoryVO> categoryVOList = new ArrayList<>();
        ParsedLongTerms catalog_agg = searchResponse.getAggregations().get("catalog_agg");
        for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
            CategoryVO categoryVO = new CategoryVO();
            Long catalogId = (Long) bucket.getKey();
//             5.1????????????id
            categoryVO.setCatalogId(catalogId);
//             5.2??????????????????????????????
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            categoryVO.setCatalogName(catalogName);
            categoryVOList.add(categoryVO);
        }
        searchResponseVO.setCategorys(categoryVOList);

//        6.???????????????????????????????????????
        List<BrandVO> brandVOList = new ArrayList<>();
        ParsedLongTerms brand_agg = searchResponse.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            BrandVO brandVO = new BrandVO();
//             6.1???????????????
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
//             6.2??????????????????
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
//             6.3????????????id
            brandVO.setBrandId((Long) bucket.getKey())
                    .setBrandName(brandName)
                    .setBrandImg(brandImg);
            brandVOList.add(brandVO);
        }
        searchResponseVO.setBrands(brandVOList);

//        7.?????????????????????????????????
        List<AttrVO> attrVOList = new ArrayList<>();
        ParsedNested attr_agg = searchResponse.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            AttrVO attrVO = new AttrVO();
//            7.1???????????????
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
//            7.2?????????????????????
            List<String> attrValueList = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(attr -> {
                return attr.getKeyAsString();
            }).collect(Collectors.toList());
            attrVO.setAttrId(bucket.getKeyAsNumber().longValue())
                    .setAttrName(attrName)
                    .setAttrValue(attrValueList);
            attrVOList.add(attrVO);
        }
        searchResponseVO.setAttrs(attrVOList);


        searchResponseVO.setPageNavs(pageNavs);

//        8.?????????????????????
        if (!CollectionUtils.isEmpty(paramDTO.getAttrs())) {
            List<NavVO> navVOS = paramDTO.getAttrs().stream().map(attr -> {
                NavVO navVO = new NavVO();
                String[] singleNav = attr.split("_");
                navVO.setNavValue(singleNav[1]);
//            8.1??????attr???????????????????????????id????????????
                for (AttrVO attrVO : attrVOList) {
                    if (attrVO.getAttrId().toString() == singleNav[0]) {
                        navVO.setNavName(attrVO.getAttrName());
                    } else {
                        navVO.setNavName("");
                    }
                }
//            8.2???????????????????????????????????????????????????????????????.??????????????????url?????????????????????
                String encode = null;
                try {
                    encode = URLEncoder.encode(attr, "UTF-8");
                    encode = encode.replace("+","20%");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String replace = paramDTO.getQueryString().replace("&attrs=" + encode, "");
                navVO.setLink("http://search.gulimall.com/list.html?"
                        + replace);
                return navVO;
            }).collect(Collectors.toList());
            searchResponseVO.setNavs(navVOS);
        }

        System.out.println(searchResponseVO);
        return searchResponseVO;
    }
}
