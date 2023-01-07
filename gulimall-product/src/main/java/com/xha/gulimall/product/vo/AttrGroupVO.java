package com.xha.gulimall.product.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xha.gulimall.product.entity.AttrEntity;
import lombok.Data;

import javax.validation.constraints.Min;
import java.util.List;

@Data
public class AttrGroupVO {
    /**
     * 分组id
     */
    @TableId
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    @Min(value = 0)
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;


    /**
     * 分类id路径
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long[] catelogPath;


    /**
     * 属性类型
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<AttrEntity> attrs;
}
