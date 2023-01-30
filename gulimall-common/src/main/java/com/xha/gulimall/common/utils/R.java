/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package com.xha.gulimall.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.xha.gulimall.common.constants.DateConstants;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据
 *
 * @author Mark sunlightcs@gmail.com
 */
public class R extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public R() {
        put("code", 0);
        put("msg", "success");
    }

    /**
     * 封装数据
     */
    public R setData(Object data) {
        return put("data", data);
    }

    /**
     * 解析数据
     * 1.@ResponseBody返回类型被封装成了Json格式
     * 2.feign接收参数时也会封装成json格式，data对象也被解析成json格式的数据（[集合对象]或{map对象}）
     * 3.将data转成json字符串格式，然后再解析成对象
     */
    public <T> T getData(TypeReference<T> type) {
        Object data = get("data");
        String jsonString = JSONObject.toJSONStringWithDateFormat(data, DateConstants.DATE_FORMAT);
        return JSONObject.parseObject(jsonString, type);
    }

    /**
     * 解析数据
     */
    public <T> T getData(String key, TypeReference<T> type) {
        Object data = get(key);
        String jsonString = JSONObject.toJSONStringWithDateFormat(data, DateConstants.DATE_FORMAT);
        return JSONObject.parseObject(jsonString, type);
    }


    public static R error() {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
    }

    public static R error(String msg) {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    public static R ok() {
        return new R();
    }

    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public Integer getCode() {
        return (Integer) this.get("code");
    }
}
