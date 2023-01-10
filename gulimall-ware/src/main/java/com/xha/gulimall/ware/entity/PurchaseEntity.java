package com.xha.gulimall.ware.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 采购信息
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:47:49
 */
@Data
@TableName("wms_purchase")
public class PurchaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 采购单id
	 */
	@TableId
	private Long id;
	/**
	 * 采购人id
	 */
	private Long assigneeId;
	/**
	 * 采购人名
	 */
	private String assigneeName;
	/**
	 * 联系方式
	 */
	private String phone;
	/**
	 * 优先级
	 */
	private Integer priority;
	/**
	 * 状态
	 */
	private Integer status;
	/**
	 * 仓库id
	 */
	private Long wareId;
	/**
	 * 总金额
	 */
	private BigDecimal amount;
	/**
	 * 创建日期
	 */
	@TableField(fill = FieldFill.INSERT)
	private Date createTime;
	/**
	 * 更新日期
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Date updateTime;

}
