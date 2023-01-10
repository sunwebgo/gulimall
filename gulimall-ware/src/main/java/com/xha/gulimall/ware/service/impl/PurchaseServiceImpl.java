package com.xha.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.enums.PurchaseEnums;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.ware.dao.PurchaseDao;
import com.xha.gulimall.ware.dto.DoneDTO;
import com.xha.gulimall.ware.dto.MergePurchaseDTO;
import com.xha.gulimall.ware.dto.PurchaseDoneDTO;
import com.xha.gulimall.ware.entity.PurchaseDetailEntity;
import com.xha.gulimall.ware.entity.PurchaseEntity;
import com.xha.gulimall.ware.entity.WareSkuEntity;
import com.xha.gulimall.ware.service.PurchaseDetailService;
import com.xha.gulimall.ware.service.PurchaseService;
import com.xha.gulimall.ware.service.WareSkuService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Resource
    private PurchaseDetailService purchaseDetailService;

    @Resource
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询未领取的采购单
     *
     * @param params 参数个数
     * @return {@link R}
     */
    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        List<Integer> statusNum = new ArrayList<>();
        statusNum.add(PurchaseEnums.NEM_BUILD.getNumber());
        statusNum.add(PurchaseEnums.DISTRIBUTE.getNumber());
        queryWrapper.in(PurchaseEntity::getStatus, statusNum);
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 合并采购单
     *
     * @return {@link R}
     */
    @Override
    public R mergePurchaseTable(MergePurchaseDTO mergePurchaseDTO) {
        long purchaseId = mergePurchaseDTO.getPurchaseId();
        if (Objects.isNull(purchaseId)) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(PurchaseEnums.NEM_BUILD.getNumber());
            save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        List<Long> items = mergePurchaseDTO.getItems();
        long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailList = items.stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            return purchaseDetailEntity
                    .setId(item)
                    .setPurchaseId(finalPurchaseId)
                    .setStatus(PurchaseEnums.DISTRIBUTE.getNumber());
        }).collect(Collectors.toList());

        boolean result = purchaseDetailService.updateBatchById(purchaseDetailList);
        return R.ok();
    }

    /**
     * 领取采购单
     *
     * @param ids id
     * @return {@link R}
     */
    @Override
    public R received(List<Long> ids) {
//        1.确定当前采购单是新建或者是已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity byId = getById(id);
            return byId;
        }).filter(item -> {
//            1.1过滤采购单是新建或已分配
            if (item.getStatus() == PurchaseEnums.NEM_BUILD.getNumber() ||
                    item.getStatus() == PurchaseEnums.DISTRIBUTE.getNumber()) {
                return true;
            }
            return false;
        }).map(item -> {
            item.setStatus(PurchaseEnums.BUYING.getNumber());
            return item;
        }).collect(Collectors.toList());

//        2.改变采购单的状态（改变为已领取）
        updateBatchById(collect);


//        3.改变采购项的状态
        collect.stream().forEach((item) -> {
            List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurcharsById(item.getId());

            List<PurchaseDetailEntity> purchaseDetailList = entities.stream().map(entitie -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                return purchaseDetailEntity
                        .setId(entitie.getId())
                        .setStatus(PurchaseEnums.BUYING.getNumber());
            }).collect(Collectors.toList());

            purchaseDetailService.updateBatchById(purchaseDetailList);
        });
        return R.ok();

    }

    /**
     * 完成采购
     *
     * @param purchaseDoneDTO 购买完成dto
     * @return {@link R}
     */
    @Override
    public R finishPurchase(PurchaseDoneDTO purchaseDoneDTO) {
//        1.获取当前采购单的id
        Long purchaseId = purchaseDoneDTO.getId();

//        2.改变采购项状态
        List<DoneDTO> items = purchaseDoneDTO.getItems();

        boolean flag = true;
        List<PurchaseDetailEntity> purchaseDetailEntityList = new ArrayList<>();
        for (DoneDTO doneDTO : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(doneDTO.getItemId());
//          2.1采购失败
            if (doneDTO.getStatus() == PurchaseEnums.HAVEEXCEPTION.getNumber()) {
                flag = false;
                purchaseDetailEntity.setStatus(doneDTO.getStatus());
            } else {
//          2.2采购成功
                purchaseDetailEntity.setStatus(PurchaseEnums.FINISH.getNumber());
//          2.3将采购的商品进行入库(wms_ware_sku)
//                2.3.1根据采购项id获取到采购项的详细信息
                PurchaseDetailEntity purchaseDetail = purchaseDetailService.getById(doneDTO.getItemId());
//                2.3.2添加库存
                wareSkuService.addStock(purchaseDetail.getSkuId(),purchaseDetail.getWareId(),purchaseDetail.getSkuNum());
            }

            purchaseDetailEntityList.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(purchaseDetailEntityList);


//        3.改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
//          3.1设置采购单状态
        purchaseEntity.setStatus(flag ? PurchaseEnums.FINISH.getNumber() : PurchaseEnums.HAVEEXCEPTION.getNumber());
        updateById(purchaseEntity);

        return R.ok();
    }
}
