package com.xha.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.xha.gulimall.auth.dto.UserLoginDTO;
import com.xha.gulimall.auth.feign.MemberFeign;
import com.xha.gulimall.common.to.UserLoginTO;
import com.xha.gulimall.common.utils.R;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @Resource
    private MemberFeign memberFeign;

    @PostMapping("/userlogin")
    public String userLogin(UserLoginDTO userLoginDTO, RedirectAttributes redirectAttributes) {

//        1.将UserLoginDTO对象转换为userLoginTO对象
        UserLoginTO userLoginTO = new UserLoginTO();
        BeanUtils.copyProperties(userLoginDTO,userLoginTO);
        R r = memberFeign.userLogin(userLoginTO);
        if (r.getCode() == 0){
            return "redirect:http://gulimall.com";
        }else{
            Map<String, String> errors = new HashMap<>();
            errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
