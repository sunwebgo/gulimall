package com.xha.gulimall.cart.to;

import lombok.Data;

@Data
public class UserInfoTO {
    private Long userId;

    private String userKey;

    private boolean tempUser = false;
}
