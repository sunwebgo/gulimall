package com.xha.gulimall.ware.dto;

import lombok.Data;

@Data
public class DoneDTO {

    /**
     * 采购项ID
     */
    private Long itemId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 原因
     */
    private String reason;
}
