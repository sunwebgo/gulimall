package com.xha.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.xha.gulimall.auth.dto.UserLoginDTO;
import com.xha.gulimall.auth.feign.MemberFeign;
import com.xha.gulimall.common.constants.CommonConstants;
import com.xha.gulimall.common.to.member.MemberTO;
import com.xha.gulimall.common.to.member.UserLoginTO;
import com.xha.gulimall.common.utils.R;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Controller
public class LoginController {

    @Resource
    private MemberFeign memberFeign;

    @PostMapping("/userlogin")
    public String userLogin(UserLoginDTO userLoginDTO,
                            RedirectAttributes redirectAttributes, HttpSession session) {

//        1.将UserLoginDTO对象转换为userLoginTO对象
        UserLoginTO userLoginTO = new UserLoginTO();
        BeanUtils.copyProperties(userLoginDTO,userLoginTO);
//        2.调用member服务，验证用户登录
        R loginResult = memberFeign.userLogin(userLoginTO);
        if (loginResult.getCode() == 0){
//        3.将当前登录对象存入session
            MemberTO memberTO = loginResult.getData(new TypeReference<MemberTO>() {
            });
            session.setAttribute(CommonConstants.LOGIN_USER,memberTO);
            return "redirect:http://gulimall.com";
        }else{
            Map<String, String> errors = new HashMap<>();
            errors.put("msg",loginResult.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }


    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
//        1.在session中获取到当前登录的用户
        Object loginUser = session.getAttribute(CommonConstants.LOGIN_USER);
        if (!Objects.isNull(loginUser)){
//        2.当前已经登录过了,跳转到首页
            return "redirect:http://gulimall.com";
        }else{
            return "login";
        }
    }
}
