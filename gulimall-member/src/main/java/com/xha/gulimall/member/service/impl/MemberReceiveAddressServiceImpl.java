package com.xha.gulimall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.to.ReceiveAddressTO;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.member.dao.MemberReceiveAddressDao;
import com.xha.gulimall.member.entity.MemberReceiveAddressEntity;
import com.xha.gulimall.member.service.MemberReceiveAddressService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("memberReceiveAddressService")
public class MemberReceiveAddressServiceImpl extends ServiceImpl<MemberReceiveAddressDao, MemberReceiveAddressEntity> implements MemberReceiveAddressService {

    @Resource
    private MemberReceiveAddressDao memberReceiveAddressDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberReceiveAddressEntity> page = this.page(
                new Query<MemberReceiveAddressEntity>().getPage(params),
                new QueryWrapper<MemberReceiveAddressEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获得接收地址列表
     *
     * @param memberId 成员身份
     * @return {@link List}<{@link ReceiveAddressTO}>
     */
    @Override
    public List<ReceiveAddressTO> getReceiveAddressList(Long memberId) {
        LambdaQueryWrapper<MemberReceiveAddressEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MemberReceiveAddressEntity::getMemberId, memberId);
        List<MemberReceiveAddressEntity> memberReceiveAddressEntities = memberReceiveAddressDao.selectList(queryWrapper);
        return memberReceiveAddressEntities.stream().map(memberReceiveAddressEntity -> {
            ReceiveAddressTO receiveAddressTO = new ReceiveAddressTO();
            BeanUtils.copyProperties(memberReceiveAddressEntity, receiveAddressTO);
            return receiveAddressTO;
        }).collect(Collectors.toList());
    }

}
