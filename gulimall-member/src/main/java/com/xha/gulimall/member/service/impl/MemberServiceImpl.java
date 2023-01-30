package com.xha.gulimall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.enums.HttpCode;
import com.xha.gulimall.common.enums.MemberEnums;
import com.xha.gulimall.common.to.UserLoginTO;
import com.xha.gulimall.common.to.UserRegisterTO;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.member.dao.MemberDao;
import com.xha.gulimall.member.entity.MemberEntity;
import com.xha.gulimall.member.exception.PhoneExitException;
import com.xha.gulimall.member.exception.UsernameExitException;
import com.xha.gulimall.member.service.MemberService;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 用户注册
     *
     * @param userRegisterTO 用户注册
     * @return {@link R}
     */
    @Override
    public void userRegister(UserRegisterTO userRegisterTO) {
//        1.判断当前用户和手机号是否已经注册过了
        checkPhoneUnique(userRegisterTO.getPhone());
        checkUsernameUnique(userRegisterTO.getUsername());

//        2.密码加密
        String password = new BCryptPasswordEncoder().encode(userRegisterTO.getPassword());

        MemberEntity member = new MemberEntity();
        member.setUsername(userRegisterTO.getUsername())
                .setMobile(userRegisterTO.getPhone())
                .setPassword(password)
                .setLevelId(MemberEnums.GENERAL_MEMBER.getLevel());
        save(member);
    }

    /**
     * 检查手机号是否存在
     */
    public void checkPhoneUnique(String phone) throws PhoneExitException {
        Integer number = baseMapper.selectCount(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getMobile, phone));
        if (number > 0) {
            throw new PhoneExitException();
        }
    }

    /**
     * 检查用户名是否存在
     */
    public void checkUsernameUnique(String username) throws UsernameExitException {
        Integer number = baseMapper.selectCount(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getUsername, username));
        if (number > 0) {
            throw new UsernameExitException();
        }
    }

    /**
     * 用户登录
     *
     * @param userLoginTO 用户登录
     * @return {@link R}
     */
    @Override
    public R userLogin(UserLoginTO userLoginTO) {
//        1.判断用户输入的是否为空
        if (StringUtils.isEmpty(userLoginTO.getUsername()) ||
                StringUtils.isEmpty(userLoginTO.getPassword())) {
            return R.error(HttpCode.DATA_EXCEPTION.getCode(), HttpCode.DATA_EXCEPTION.getMessage());
        }
//        2.根据用户名或者手机号查询数据库
        LambdaQueryWrapper<MemberEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(MemberEntity::getUsername, userLoginTO.getUsername())
                .or()
                .eq(MemberEntity::getMobile, userLoginTO.getUsername());
        MemberEntity member = getOne(queryWrapper);
        if (Objects.isNull(member)) {
//         2.1当前用户不存在
            return R.error(HttpCode.USER_NOT_EXIST_EXCEPTION.getCode(), HttpCode.USER_NOT_EXIST_EXCEPTION.getMessage());
        } else {
//         3.验证密码
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            if (!bCryptPasswordEncoder.matches(userLoginTO.getPassword(), member.getPassword())) {
//                3.1密码错误
                return R.error(HttpCode.PASSWORD_EXCEPTION.getCode(), HttpCode.PASSWORD_EXCEPTION.getMessage());
            }
        }
        return R.ok();
    }

}
