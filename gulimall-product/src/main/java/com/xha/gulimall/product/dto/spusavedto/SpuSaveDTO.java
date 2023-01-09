/**
  * Copyright 2023 bejson.com
  */
package com.xha.gulimall.product.dto.spusavedto;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Auto-generated: 2023-01-08 11:11:14
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class SpuSaveDTO {

    /**
     * spu名字
     */
    private String spuName;
    /**
     * spu描述
     */
    private String spuDescription;
    /**
     * 目录id
     */
    private Long catelogId;
    /**
     * 品牌标识
     */
    private Long brandId;
    /**
     * 重量
     */
    private BigDecimal weight;
    /**
     * 发布状态
     */
    private int publishStatus;


    /**
     * spu详细信息表
     */
    private List<String> decript;


    /**
     * spu图片表
     */
    private List<String> images;

    /**
     * 积分
     */
    private Bounds bounds;

    /**
     * spu的基本属性
     */
    private List<BaseAttrs> baseAttrs;

    private List<Skus> skus;

}
