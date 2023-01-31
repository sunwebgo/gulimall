package com.xha.gulimall.auth.service.Impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.TypeReference;
import com.xha.gulimall.auth.feign.MemberFeign;
import com.xha.gulimall.auth.service.OAuthService;
import com.xha.gulimall.common.enums.HttpCode;
import com.xha.gulimall.common.to.GiteeResponseTO;
import com.xha.gulimall.common.to.MemberTO;
import com.xha.gulimall.common.utils.HttpUtils;
import com.xha.gulimall.common.utils.R;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "oauth.gitee")
@Service
public class OAuthServiceImpl implements OAuthService {

    @Resource
    private MemberFeign memberFeign;


    private String host;

    private String path;

    private String grant_type;

    private String client_id;

    private String redirect_uri;

    private String client_secret;

    /**
     * gitee oauth
     *
     * @param code 代码
     * @return {@link String}
     */
    @Override
    public String giteeOAuth(String code) {
        Map<String, String> querys = new HashMap<>();
        querys.put("grant_type", grant_type);
        querys.put("code", code);
        querys.put("client_id", client_id);
        querys.put("redirect_uri", redirect_uri);
        querys.put("client_secret", client_secret);
        try {
//            1.根据code码获取access_toekn
            HttpResponse response = HttpUtils.doPost(
                    host, path, "post", new HashMap<String, String>(), querys, new HashMap<String, String>());
            if (response.getStatusLine().getStatusCode() == HttpCode.STATUS_NORMAL.getCode()) {
//            2.获取到了access_token
//            3.通过EntityUtils将HttpEntity对象转为json数据
                String httpEntityStr = EntityUtils.toString(response.getEntity());
//            4.将json数据转为GiteeResponseEntity对象
                GiteeResponseTO giteeResponseTO = JSONUtil.toBean(httpEntityStr, GiteeResponseTO.class);
//            5.调用member服务，用户注册或者登录
                R oauthResult = memberFeign.userOAuthGiteeLogin(giteeResponseTO);
                if (oauthResult.getCode() == 0){
                    MemberTO data = oauthResult.getData(new TypeReference<MemberTO>() {
                    });
                    System.out.println(data);

//                    TODO 使用SpringSession处理数据共享问题
                }else{
//                    第三方认证登录失败
                    return "redirect:http://auth.gulimall.com/login.html";
                }

            } else {
                return "redirect:http://auth.gulimall.com/login.html";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:http://gulimall.com";
    }
}
