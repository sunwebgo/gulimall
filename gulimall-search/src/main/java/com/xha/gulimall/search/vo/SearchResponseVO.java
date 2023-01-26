package com.xha.gulimall.search.vo;

import com.xha.gulimall.common.to.es.SkuInfoES;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SearchResponseVO {

    /**
     * 商品信息(存储在es当中的商品信息)
     */
    private List<SkuInfoES> products;

    /**
     * 品牌列表
     */
    private List<BrandVO> brands;

    /**
     * 属性列表
     */
    private List<AttrVO> attrs;

    /**
     * 类别
     */
    private List<CategoryVO> categorys;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页码
     */
    private Integer totalPages;

    /**
     * 导航页码
     */
    private List<Integer> pageNavs;

    /**
     * 面包屑导航
     */
    private List<NavVO> navs = new ArrayList<>();

}
