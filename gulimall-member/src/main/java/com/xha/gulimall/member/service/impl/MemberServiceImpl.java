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
import com.xha.gulimall.common.to.member.GiteeResponseTO;
import com.xha.gulimall.common.to.member.UserLoginTO;
import com.xha.gulimall.common.to.member.UserRegisterTO;
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
     * ????????????
     *
     * @param userRegisterTO ????????????
     */
    @Override
    public void userRegister(UserRegisterTO userRegisterTO) {
//        1.??????????????????????????????????????????????????????
        checkPhoneUnique(userRegisterTO.getPhone());
        checkUsernameUnique(userRegisterTO.getUsername());

//        2.????????????
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
     * ???????????????????????????
     */
    public void checkPhoneUnique(String phone) throws PhoneExitException {
        Integer number = baseMapper.selectCount(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getMobile, phone));
        if (number > 0) {
            throw new PhoneExitException();
        }
    }

    /**
     * ???????????????????????????
     */
    public void checkUsernameUnique(String username) throws UsernameExitException {
        Integer number = baseMapper.selectCount(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getUsername, username));
        if (number > 0) {
            throw new UsernameExitException();
        }
    }

    /**
     * ????????????
     *
     * @param userLoginTO ????????????
     * @return {@link R}
     */
    @Override
    public R userLogin(UserLoginTO userLoginTO) {
//        1.?????????????????????????????????
        if (StringUtils.isEmpty(userLoginTO.getUsername()) ||
                StringUtils.isEmpty(userLoginTO.getPassword())) {
            return R.error(HttpCode.DATA_EXCEPTION.getCode(), HttpCode.DATA_EXCEPTION.getMessage());
        }
//        2.?????????????????????????????????????????????
        LambdaQueryWrapper<MemberEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(MemberEntity::getUsername, userLoginTO.getUsername())
                .or()
                .eq(MemberEntity::getMobile, userLoginTO.getUsername());
        MemberEntity member = getOne(queryWrapper);
        if (Objects.isNull(member)) {
//         2.1?????????????????????
            return R.error(HttpCode.USER_NOT_EXIST_EXCEPTION.getCode(), HttpCode.USER_NOT_EXIST_EXCEPTION.getMessage());
        } else {
//         3.????????????
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            if (!bCryptPasswordEncoder.matches(userLoginTO.getPassword(), member.getPassword())) {
//                3.1????????????
                return R.error(HttpCode.PASSWORD_EXCEPTION.getCode(), HttpCode.PASSWORD_EXCEPTION.getMessage());
            }
        }
       return R.ok().setData(member);
    }


    /**
     * Gitee?????????????????????
     *
     * @return {@link R}
     */
    @Override
    public MemberEntity userOAuthGiteeLogin(GiteeResponseTO giteeResponseTO) {
        //        1.????????????????????????????????????????????????
        //          1.1??????token????????????id
        Map<String, String> querys = new HashMap<>();
        querys.put("access_token", giteeResponseTO.getAccess_token());
        MemberEntity member = null;
        try {
            HttpResponse response = HttpUtils.doGet(host, path, "get", new HashMap<String, String>(), querys);
            String userInfo = EntityUtils.toString(response.getEntity());
            //            ?????????????????????GiteeUserInfo??????
            GiteeUserInfo giteeUserInfo = JSONUtil.toBean(userInfo, GiteeUserInfo.class);
            //            ??????gitee???????????????id??????????????????????????????
            LambdaQueryWrapper<MemberEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MemberEntity::getThirdId, giteeUserInfo.getId());
            member = getOne(queryWrapper);
            if (Objects.isNull(member)) {
                //                ???????????????????????????????????????
                MemberEntity memberEntity = new MemberEntity();
                memberEntity.setThirdId(String.valueOf(giteeUserInfo.getId()))
                        .setSourceType(UserOriginName.GITEE)
                        .setLevelId(MemberEnums.GENERAL_MEMBER.getLevel())
                        .setUsername(giteeUserInfo.getLogin())
                        .setEmail(giteeUserInfo.getEmail())
                        .setHeader(giteeUserInfo.getAvatar_url());
                save(memberEntity);
                //                ????????????access_token???expire_in????????????
                stringRedisTemplate.opsForValue().set(
                        CacheConstants.GITEE_LOGIN_ACCESS_TOKEN_CACHE + memberEntity.getThirdId(),
                        giteeResponseTO.getAccess_token(),
                        NumberConstants.ACCESS_TOKEN_EXPIRE_TIME,
                        TimeUnit.SECONDS);
                member = memberEntity;
            } else {
                //               ????????????????????????????????????access_token???????????????
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
