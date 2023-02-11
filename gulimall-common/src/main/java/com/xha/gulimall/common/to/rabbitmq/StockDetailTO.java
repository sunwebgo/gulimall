package com.xha.gulimall.common.to.rabbitmq;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class StockDetailTO {

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;

    /**
     * 仓库ID
     */
    private Long wareId;

    /**
     * 锁定状态
     */
    private Integer lockStatus;
}
