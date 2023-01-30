package com.xha.gulimall.member.exception;

public class UsernameExitException extends RuntimeException{
    public UsernameExitException() {
        super("用户名已经存在");
    }
}
