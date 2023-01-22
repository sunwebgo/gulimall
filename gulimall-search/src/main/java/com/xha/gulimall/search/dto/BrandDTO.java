package com.xha.gulimall.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 品牌dto
 *
 * @author Xu Huaiang
 * @date 2023/01/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandDTO {
    private Long brandId;

    private String brandName;

    private String brandImg;
}
