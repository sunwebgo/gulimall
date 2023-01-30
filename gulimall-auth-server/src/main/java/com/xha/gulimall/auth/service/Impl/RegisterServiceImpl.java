package com.xha.gulimall.auth.service.Impl;

import cn.hutool.core.util.RandomUtil;
import com.xha.gulimall.auth.constants.CacheConstants;
import com.xha.gulimall.auth.constants.NumberConstants;
import com.xha.gulimall.auth.dto.UserRegisterDTO;
import com.xha.gulimall.auth.feign.ThirdServerFeign;
import com.xha.gulimall.auth.service.RegisterService;
import com.xha.gulimall.common.enums.HttpCode;
import com.xha.gulimall.common.utils.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RegisterServiceImpl implements RegisterService {

    @Resource
    private ThirdServerFeign thirdServerFeign;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送验证码
     *
     * @param phone 电话
     * @return {@link R}
     */
    @Override
    public R sendCaptcha(String phone) {
//        TODO 接口防刷

        if (StringUtils.isEmpty(phone)) {
            return R.error().put("msg", "手机号不能为空");
        }
//        1.获取到对应缓存的过期时间
        Long expireTime = stringRedisTemplate.opsForValue().getOperations().getExpire(CacheConstants.PHONE_CAPTCHA + phone);
//        2.缓存不存在(即还没有发送验证码)重建缓存
        if (expireTime == NumberConstants.EXPIRE_TIME_STATUS) {
//            2.1生成验证码
            createCaptcha(phone);
        } else {
            if (expireTime >= NumberConstants.EXPIRE_TIME) {
                return R.error(HttpCode.CHPRCHE_EXCEPTION.getCode(), HttpCode.CHPRCHE_EXCEPTION.getMessage());
            } else {
                //            2.1生成验证码
                createCaptcha(phone);
            }
        }
        return R.ok();
    }



    /**
     * 创建验证码
     */
    public void createCaptcha(String phone) {
        String captcha = RandomUtil.randomNumbers(NumberConstants.CAPTCHA_LENGTH);
        stringRedisTemplate.opsForValue().set(CacheConstants.PHONE_CAPTCHA + phone, captcha, 10, TimeUnit.MINUTES);
        thirdServerFeign.sendCaptcha(phone, captcha);
    }
}
