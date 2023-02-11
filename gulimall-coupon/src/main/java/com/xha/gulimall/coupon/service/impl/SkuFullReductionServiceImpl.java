package com.xha.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.constants.NumberConstants;
import com.xha.gulimall.common.to.member.MemberPrice;
import com.xha.gulimall.common.to.product.SkuReductionTO;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.coupon.dao.SkuFullReductionDao;
import com.xha.gulimall.coupon.entity.MemberPriceEntity;
import com.xha.gulimall.coupon.entity.SkuFullReductionEntity;
import com.xha.gulimall.coupon.entity.SkuLadderEntity;
import com.xha.gulimall.coupon.service.MemberPriceService;
import com.xha.gulimall.coupon.service.SkuFullReductionService;
import com.xha.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Resource
    private SkuLadderService skuLadderService;

    @Resource
    private MemberPriceService memberPriceService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存sku打折满减等信息
     *
     * @param skuReductionTO sku减少
     * @return {@link R}
     */
    @Override
    public void saveSkuReduction(SkuReductionTO skuReductionTO) {
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
//        1.保存sku的优惠信息
//          1.1将SkuReductionTO对象转换为SkuLadderEntity对象
        BeanUtils.copyProperties(skuReductionTO, skuLadderEntity);
        skuLadderEntity.setAddOther(skuReductionTO.getCountStatus());
        if (skuLadderEntity.getFullCount() > NumberConstants.ZERO){
            skuLadderService.save(skuLadderEntity);
        }

//        2.保存sku的满减信息
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTO, skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuReductionTO.getCountStatus());
        if (skuFullReductionEntity.getFullPrice().compareTo(BigDecimal.ZERO) == 1){
            save(skuFullReductionEntity);
        }

//        3.保存会员价格
        List<MemberPrice> memberPrices = skuReductionTO.getMemberPrice();
        if (!CollectionUtils.isEmpty(memberPrices)){
            List<MemberPriceEntity> memberPriceList = memberPrices.stream().map(memberPrice -> {
                MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                return memberPriceEntity
                        .setSkuId(skuReductionTO.getSkuId())
                        .setMemberLevelId(memberPrice.getId())
                        .setMemberLevelName(memberPrice.getName())
                        .setMemberPrice(memberPrice.getPrice())
                        .setAddOther(1);
            }).filter(memberPriceEntity -> {
                return memberPriceEntity.getMemberPrice().compareTo(BigDecimal.ZERO) == 1;
            }).collect(Collectors.toList());
            memberPriceService.saveBatch(memberPriceList);
        }
    }

}
