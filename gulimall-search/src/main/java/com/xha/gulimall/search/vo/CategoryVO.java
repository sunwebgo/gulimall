package com.xha.gulimall.search.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 类别dto
 *
 * @author Xu Huaiang
 * @date 2023/01/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryVO {
    /**
     * 分类id
     */
    private Long catalogId;

    /**
     * 分类名称
     */
    private String catalogName;
}
