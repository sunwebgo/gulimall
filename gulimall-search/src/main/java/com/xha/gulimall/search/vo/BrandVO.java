package com.xha.gulimall.search.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 品牌dto
 *
 * @author Xu Huaiang
 * @date 2023/01/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BrandVO {
    private Long brandId;

    private String brandName;

    private String brandImg;
}
