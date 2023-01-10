package com.xha.gulimall.ware.dto;

import lombok.Data;

import java.util.List;

@Data
public class MergePurchaseDTO {

    /**
     * 采购单ID
     */
    private long purchaseId;

    private List<Long> items;
}
