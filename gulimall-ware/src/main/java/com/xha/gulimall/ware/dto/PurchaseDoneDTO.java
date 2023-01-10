package com.xha.gulimall.ware.dto;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseDoneDTO {

    /**
     * 采购单id
     */
    private Long id;

    private List<DoneDTO> items;
}
