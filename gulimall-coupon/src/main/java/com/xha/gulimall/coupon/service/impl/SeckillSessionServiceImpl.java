package com.xha.gulimall.coupon.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.to.coupon.SeckillSessionTO;
import com.xha.gulimall.common.to.coupon.SeckillSkuRelationTO;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.coupon.dao.SeckillSessionDao;
import com.xha.gulimall.coupon.dao.SeckillSkuRelationDao;
import com.xha.gulimall.coupon.entity.SeckillSessionEntity;
import com.xha.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.xha.gulimall.coupon.service.SeckillSessionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Resource
    private SeckillSessionDao seckillSessionDao;

    @Resource
    private SeckillSkuRelationDao seckillSkuRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询秒杀场次
     *
     * @return {@link List}<{@link SeckillSessionTO}>
     */
    @Override
    public List<SeckillSessionTO> getSeckillSession() {
//        1.时间的格式化表示
        LocalDateTime beginTime = LocalDateTimeUtil.beginOfDay(LocalDateTime.now());
//          1.1算上今天，向后延长2天，一共三天时间
        LocalDateTime endTime = LocalDateTimeUtil
                .offset(LocalDateTimeUtil.endOfDay(LocalDateTime.now()), 2, ChronoUnit.DAYS);
//          1.2格式化
        String beginFormatTime = LocalDateTimeUtil.format(beginTime, DatePattern.NORM_DATETIME_PATTERN);
        String endFormatTime = LocalDateTimeUtil.format(endTime, DatePattern.NORM_DATETIME_PATTERN);
//        2.获取三天(含今天)的秒杀活动集合
        LambdaQueryWrapper<SeckillSessionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(SeckillSessionEntity::getStartTime, beginFormatTime, endFormatTime);
        List<SeckillSessionEntity> seckillSessionList = seckillSessionDao.selectList(queryWrapper);
        List<SeckillSessionTO> seckillSessionTOList = null;
        if (!CollectionUtils.isEmpty(seckillSessionList)) {
            seckillSessionTOList = seckillSessionList.stream().map(seckillSessionEntity -> {
                SeckillSessionTO seckillSessionTO = new SeckillSessionTO();
                BeanUtils.copyProperties(seckillSessionEntity, seckillSessionTO);
                return seckillSessionTO;
            }).collect(Collectors.toList());
//        3.查询秒杀活动关联的商品集合
//          3.1查询出所有的关联列表
            List<SeckillSkuRelationEntity> seckillSkuRelationList = seckillSkuRelationDao.selectList(null);
            seckillSessionTOList = seckillSessionTOList.stream()
                    .map(seckillSessionTO -> {
                        seckillSessionTO
                                .setRelationSkus(getRelationSkuList(seckillSkuRelationList, seckillSessionTO));
                        return seckillSessionTO;
                    }).collect(Collectors.toList());
        }

        return seckillSessionTOList;
    }

    /**
     * 得到sku关系列表
     *
     * @param seckillSkuRelationList 秒杀sku关系列表
     * @param seckillSessionTO       秒杀会话
     * @return {@link List}<{@link SeckillSkuRelationTO}>
     */
    private List<SeckillSkuRelationTO> getRelationSkuList(List<SeckillSkuRelationEntity> seckillSkuRelationList, SeckillSessionTO seckillSessionTO) {
        List<SeckillSkuRelationTO> seckillSkuRelationTOList = seckillSkuRelationList.stream()
                .filter(seckillSkuRelation ->
                        seckillSkuRelation.getPromotionSessionId().equals(seckillSessionTO.getId()))
                .map(seckillSkuRelationEntity -> {
                    SeckillSkuRelationTO seckillSkuRelationTO = new SeckillSkuRelationTO();
                    BeanUtils.copyProperties(seckillSkuRelationEntity, seckillSkuRelationTO);
                    return seckillSkuRelationTO;
                })
                .collect(Collectors.toList());
        return seckillSkuRelationTOList;
    }

}
