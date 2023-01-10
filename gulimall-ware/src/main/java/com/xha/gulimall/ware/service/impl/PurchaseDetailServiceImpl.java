package com.xha.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.ware.dao.PurchaseDetailDao;
import com.xha.gulimall.ware.entity.PurchaseDetailEntity;
import com.xha.gulimall.ware.service.PurchaseDetailService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                new QueryWrapper<PurchaseDetailEntity>()
        );

        return new PageUtils(page);
    }


    /**
     * 查询采购需求
     *
     * @param params 参数个数
     * @return {@link PageUtils}
     */
    @Override
    public PageUtils queryPageByConsition(Map<String, Object> params) {
//        1.获取到检索字段
        String key = (String) params.get("key");
//        2.获取到查询参数
        String status = (String) params.get("status");
        String wareId = (String) params.get("wareId");

        LambdaQueryWrapper<PurchaseDetailEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (!StringUtils.isEmpty(key)) {
            queryWrapper
                    .eq(PurchaseDetailEntity::getPurchaseId, key)
                    .or()
                    .eq(PurchaseDetailEntity::getSkuId, key);
        }
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq(PurchaseDetailEntity::getStatus, status);
        }
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq(PurchaseDetailEntity::getWareId, wareId);
        }

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurcharsById(Long id) {
        List<PurchaseDetailEntity> purchaseList = list(new LambdaQueryWrapper<PurchaseDetailEntity>()
                .eq(PurchaseDetailEntity::getPurchaseId, id));
        return purchaseList;
    }

}
