package com.xha.gulimall.common.to.coupon;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SeckillSessionTO {
    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 秒杀活动关联的商品列表
     */
    private List<SeckillSkuRelationTO> relationSkus;
}
