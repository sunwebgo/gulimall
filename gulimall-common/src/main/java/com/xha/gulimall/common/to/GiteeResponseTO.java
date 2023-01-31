package com.xha.gulimall.common.to;

import lombok.Data;

/**
 * gitee响应实体
 *
 * @author Xu Huaiang
 * @date 2023/01/31
 */
@Data
public class GiteeResponseTO {
    private String access_token;

    private String token_type;

    private int expires_in;

    private String refresh_token;

    private String scope;

    private int created_at;
}
