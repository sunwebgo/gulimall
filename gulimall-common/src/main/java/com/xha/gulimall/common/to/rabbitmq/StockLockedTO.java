package com.xha.gulimall.common.to.rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class StockLockedTO {

    /**
     * 库存工作单id
     */
    private Long id;

    /**
     * 库存工作单详情
     */
    private StockDetailTO detail;




}
