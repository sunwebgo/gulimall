package com.xha.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.ware.dao.WareInfoDao;
import com.xha.gulimall.ware.entity.WareInfoEntity;
import com.xha.gulimall.ware.service.WareInfoService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                new QueryWrapper<WareInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据条件查询仓库列表
     *
     * @param params 参数个数
     * @return {@link R}
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
//        1.获取到检索关键字
        String key = (String) params.get("key");

        LambdaQueryWrapper<WareInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(key)){
            queryWrapper
                    .like(WareInfoEntity::getName,key)
                    .or()
                    .like(WareInfoEntity::getAddress,key)
                    .or()
                    .eq(WareInfoEntity::getAreacode,key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

}
