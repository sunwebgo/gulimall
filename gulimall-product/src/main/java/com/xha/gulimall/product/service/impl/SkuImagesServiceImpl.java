package com.xha.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.product.dao.SkuImagesDao;
import com.xha.gulimall.product.entity.SkuImagesEntity;
import com.xha.gulimall.product.service.SkuImagesService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


@Service("skuImagesService")
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesDao, SkuImagesEntity> implements SkuImagesService {

    @Resource
    private SkuImagesDao skuImagesDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuImagesEntity> page = this.page(
                new Query<SkuImagesEntity>().getPage(params),
                new QueryWrapper<SkuImagesEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据skuID得到sku图像信息
     *
     * @param skuId sku id
     * @return {@link List}<{@link SkuImagesEntity}>
     */
    @Override
    public List<SkuImagesEntity> getSkuImageInfo(Long skuId) {
        LambdaQueryWrapper<SkuImagesEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuImagesEntity::getSkuId,skuId);
        List<SkuImagesEntity> skuImagesEntities = skuImagesDao.selectList(queryWrapper);
        return skuImagesEntities;
    }

}
