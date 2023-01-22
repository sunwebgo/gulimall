package com.xha.gulimall.search.dto;

import com.xha.gulimall.common.to.es.SkuInfoES;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {

    /**
     * 商品信息(存储在es当中的商品信息)
     */
    private List<SkuInfoES> products;

    /**
     * 品牌列表
     */
    private List<BrandDTO> brands;

    /**
     * 属性列表
     */
    private List<AttrDTO> attrs;

    /**
     * 类别
     */
    private List<CategoryDTO> categorys;

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

}
