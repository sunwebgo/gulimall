package com.xha.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.xha.gulimall.auth.constants.CacheConstants;
import com.xha.gulimall.auth.dto.UserRegisterDTO;
import com.xha.gulimall.auth.feign.MemberFeign;
import com.xha.gulimall.auth.service.RegisterService;
import com.xha.gulimall.common.to.UserRegisterTO;
import com.xha.gulimall.common.utils.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class RegisterController {


    @Resource
    private RegisterService registerService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MemberFeign memberFeign;


    /**
     * 发送验证码
     *
     * @param phone 电话
     * @return {@link R}
     */
    @ResponseBody
    @PostMapping("/sms/sendcaptcha")
    public R sendCaptcha(String phone) {
        return registerService.sendCaptcha(phone);
    }

    /**
     * 用户注册
     *
     * @return {@link String}
     */
    @PostMapping("/register")
    public String userRegister(UserRegisterDTO userRegisterDTO,
                               RedirectAttributes redirectAttributes) {
        if (StringUtils.isEmpty(userRegisterDTO.getUsername()) ||
                StringUtils.isEmpty(userRegisterDTO.getPassword()) ||
                StringUtils.isEmpty(userRegisterDTO.getPhone()) || StringUtils.isEmpty(userRegisterDTO.getCaptcha())){
            return "redirect:http://auth.gulimall.com/register.html";
        }
//        校验验证码
        String cacheCaptcha = stringRedisTemplate.opsForValue()
                .get(CacheConstants.PHONE_CAPTCHA + userRegisterDTO.getPhone());
        if (cacheCaptcha.equals(userRegisterDTO.getCaptcha())) {
//            删除验证码
            stringRedisTemplate.delete(CacheConstants.PHONE_CAPTCHA + userRegisterDTO.getPhone());
//            将UserRegisterDTO对象转换为UserRegisterTO对象
            UserRegisterTO userRegisterTO = new UserRegisterTO();
            BeanUtils.copyProperties(userRegisterDTO, userRegisterTO);
//            调用会员服务完成用户注册
            R r = memberFeign.userRegister(userRegisterTO);
            if (r.getCode() == 0) {
//                成功跳转到登录页面
                return "redirect:http://auth.gulimall.com/login.html";
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("msg",r.getData(new TypeReference<String>(){}));
                redirectAttributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.gulimall.com/register.html";
            }

        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/register.html";
        }
    }


}

