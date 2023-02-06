package com.xha.gulimall.member.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.constants.CacheConstants;
import com.xha.gulimall.common.constants.NumberConstants;
import com.xha.gulimall.common.enums.HttpCode;
import com.xha.gulimall.common.enums.MemberEnums;
import com.xha.gulimall.common.to.GiteeResponseTO;
import com.xha.gulimall.common.to.UserLoginTO;
import com.xha.gulimall.common.to.UserRegisterTO;
import com.xha.gulimall.common.utils.HttpUtils;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.member.constants.UserOriginName;
import com.xha.gulimall.member.dao.MemberDao;
import com.xha.gulimall.member.entity.MemberEntity;
import com.xha.gulimall.member.exception.PhoneExitException;
import com.xha.gulimall.member.exception.UsernameExitException;
import com.xha.gulimall.member.pojo.GiteeUserInfo;
import com.xha.gulimall.member.service.MemberService;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Data
@Service("memberService")
@ConfigurationProperties(prefix = "oauth.gitee")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private String host;

    private String path;


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
                .setSourceType(UserOriginName.PHONE)
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
       return R.ok().setData(member);
    }


    /**
     * Gitee第三方用户登录
     *
     * @return {@link R}
     */
    @Override
    public MemberEntity userOAuthGiteeLogin(GiteeResponseTO giteeResponseTO) {
        //        1.判断当前第三方用户是否已经注册过
        //          1.1根据token查询用户id
        Map<String, String> querys = new HashMap<>();
        querys.put("access_token", giteeResponseTO.getAccess_token());
        MemberEntity member = null;
        try {
            HttpResponse response = HttpUtils.doGet(host, path, "get", new HashMap<String, String>(), querys);
            String userInfo = EntityUtils.toString(response.getEntity());
            //            将用户信息转为GiteeUserInfo对象
            GiteeUserInfo giteeUserInfo = JSONUtil.toBean(userInfo, GiteeUserInfo.class);
            //            根据gitee提供的唯一id查询当前用户是否存在
            LambdaQueryWrapper<MemberEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MemberEntity::getThirdId, giteeUserInfo.getId());
            member = getOne(queryWrapper);
            if (Objects.isNull(member)) {
                //                当前用户为首次登录，先注册
                MemberEntity memberEntity = new MemberEntity();
                memberEntity.setThirdId(String.valueOf(giteeUserInfo.getId()))
                        .setSourceType(UserOriginName.GITEE)
                        .setLevelId(MemberEnums.GENERAL_MEMBER.getLevel())
                        .setUsername(giteeUserInfo.getLogin())
                        .setEmail(giteeUserInfo.getEmail())
                        .setHeader(giteeUserInfo.getAvatar_url());
                save(memberEntity);
                //                将当前的access_token和expire_in存入缓存
                stringRedisTemplate.opsForValue().set(
                        CacheConstants.GITEE_LOGIN_ACCESS_TOKEN_CACHE + memberEntity.getThirdId(),
                        giteeResponseTO.getAccess_token(),
                        NumberConstants.ACCESS_TOKEN_EXPIRE_TIME,
                        TimeUnit.SECONDS);
                member = memberEntity;
            } else {
                //               重制当前用户对应缓存中的access_token的超时时间
                stringRedisTemplate.opsForValue().set(
                        CacheConstants.GITEE_LOGIN_ACCESS_TOKEN_CACHE + member.getThirdId(),
                        giteeResponseTO.getAccess_token(),
                        NumberConstants.ACCESS_TOKEN_EXPIRE_TIME,
                        TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return member;
    }

}
