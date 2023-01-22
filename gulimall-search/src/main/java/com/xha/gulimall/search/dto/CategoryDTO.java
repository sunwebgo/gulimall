package com.xha.gulimall.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 类别dto
 *
 * @author Xu Huaiang
 * @date 2023/01/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {
    /**
     * 分类id
     */
    private Long catalogId;

    /**
     * 分类名称
     */
    private String catalogName;
}
