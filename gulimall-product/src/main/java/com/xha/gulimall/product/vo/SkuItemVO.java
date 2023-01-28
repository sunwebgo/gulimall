package com.xha.gulimall.product.vo;

import com.xha.gulimall.product.entity.SkuImagesEntity;
import com.xha.gulimall.product.entity.SkuInfoEntity;
import com.xha.gulimall.product.entity.SpuInfoDescEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SkuItemVO {
    /**
     * 1.获取到sku的基本信息 pms_sku_info
     */
    private SkuInfoEntity skuInfoEntity;

    /**
     * 2.获取到sku的图片信息 pms_sku_images
     */
    List<SkuImagesEntity> images;

    /**
     * 3.获取到spu的销售属性组合
     */
    private List<SkuItemSaleAttrVO> saleAttr;

    /**
     * 4.获取到spu的介绍信息
     */
    private SpuInfoDescEntity desp;

    /**
     * 5.获取到spu的介绍信息
     */
    private List<SpuItemAttrGroupVO> groupVos;
}
