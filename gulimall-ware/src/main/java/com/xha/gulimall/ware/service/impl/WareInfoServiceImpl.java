package com.xha.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.to.member.ReceiveAddressTO;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.ware.dao.WareInfoDao;
import com.xha.gulimall.ware.entity.WareInfoEntity;
import com.xha.gulimall.ware.feign.MemberFeignService;
import com.xha.gulimall.ware.service.WareInfoService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Resource
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                new QueryWrapper<>()
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
        if (!StringUtils.isEmpty(key)) {
            queryWrapper
                    .like(WareInfoEntity::getName, key)
                    .or()
                    .like(WareInfoEntity::getAddress, key)
                    .or()
                    .eq(WareInfoEntity::getAreacode, key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    /**
     * 根据用户的收货地址得到运费
     *
     * @return {@link R}
     */
    @Override
    public ReceiveAddressTO getUserInfo(Long addrId) {
//        1.调用远程服务,根据addrId查询当前一条收货人信息
        ReceiveAddressTO receiveAddress = memberFeignService.getReceiveAddress(addrId);
        if (!Objects.isNull(receiveAddress)) {
            return receiveAddress;
        } else {
            return null;
        }
    }

}
