package com.xha.gulimall.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 封装页面传递的查询参数
 *
 * @author Xu Huaiang
 * @date 2023/01/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class    ParamDTO {

    /**
     * 检索关键字
     */
    private String keyword;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     * 排序条件
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_asc/desc
     */
    private String sort;

    /**
     * 品牌id
     */
    private List<Long> brandId;

    /**
     * 按照属性进行筛选
     */
    private List<String> attrs;

    /**
     * 是否有货
     * hasStock 0/1(是否有货)
     */
    private Integer hasStock;

    /**
     * 价格区间
     */
    private String skuPrice;

    /**
     * 页码
     */
    private Integer pageNum = 1;


}
