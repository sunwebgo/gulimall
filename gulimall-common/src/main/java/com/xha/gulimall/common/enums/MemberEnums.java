package com.xha.gulimall.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberEnums {
    GENERAL_MEMBER(1L,"普通会员");

    private Long level;
    private String comment;
}
