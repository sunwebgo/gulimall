package com.xha.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catelog2VO {

    private String id;

    private String name;
    /**
     * 父分类id
     */
    private String catalog1Id;

    /**
     * 三级子分类
     */
    private List<Catelog3VO> catalog3List;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Catelog3VO {
        private String catalog2Id;
        private String id;
        private String name;
    }
}
