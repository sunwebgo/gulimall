package com.xha.gulimall.member.exception;

public class PhoneExitException extends RuntimeException{
    public PhoneExitException() {
        super("当前手机号已经被注册");
    }
}
