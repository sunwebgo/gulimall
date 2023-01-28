package com.xha.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.product.dao.SkuInfoDao;
import com.xha.gulimall.product.dao.SkuSaleAttrValueDao;
import com.xha.gulimall.product.entity.SkuInfoEntity;
import com.xha.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.xha.gulimall.product.service.SkuSaleAttrValueService;
import com.xha.gulimall.product.vo.SkuItemSaleAttrVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Resource
    private SkuInfoDao skuInfoDao;

    @Resource
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }


    /**
     * 获取到spu的销售属性组合
     *
     * @param spuId spu id
     * @return {@link List}<{@link SkuItemSaleAttrVO}>
     */
    @Override
    public List<SkuItemSaleAttrVO> getSaleAttrBySpuId(Long spuId) {
        return skuSaleAttrValueDao.getSaleAttrBySpuId(spuId);
    }

}
