package com.xha.gulimall.common.validator.annotationvalidator;

import com.xha.gulimall.common.validator.annotation.ListValue;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class ListValueValidator implements ConstraintValidator<ListValue,Integer> {

    private Set<Integer> set = new HashSet<>();

//    初始化方法
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] value = constraintAnnotation.value();
        for (int val:value){
            set.add(val);
        }
    }

    /**
     * 是有效
     *
     * @param value   需要校验的值
     * @param context 上下文
     * @return boolean
     *///    判断是否校验成功
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return set.contains(value);
    }
}
