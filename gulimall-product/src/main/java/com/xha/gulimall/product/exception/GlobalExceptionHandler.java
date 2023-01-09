package com.xha.gulimall.product.exception;


import com.xha.gulimall.common.enums.HttpCode;
import com.xha.gulimall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理类
 *
 * @author Xu Huaiang
 * @date 2023/01/04
 */
@Slf4j
//指定要处理那些包下的异常
@RestControllerAdvice(basePackages = "com.xha.gulimall.product")
public class GlobalExceptionHandler {

    /**
     * 数据校验异常处理
     *
     * @param e e
     * @return {@link R}
     *///    使用@ExceptionHandler指定要处理哪些异常
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题：" + e.getMessage() + "异常类型是：" + e.getClass());
//        得到数据校验的错误结果
        BindingResult bindingResult = e.getBindingResult();
        Map<String,String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((fieldError -> {
            errorMap.put(fieldError.getField(),fieldError.getDefaultMessage());
        }));
        return R.error(HttpCode.VALID_EXCEPTION.getCode(), HttpCode.VALID_EXCEPTION.getMessage()).put("data",errorMap);
    }

//    /**
//     * 全局异常处理
//     *
//     * @return {@link R}
//     */
//    @ExceptionHandler(value = Throwable.class)
//    public R handlerException(Throwable e){
//        log.error("出现全局异常：" + e.getMessage() + "异常类型是：" + e.getClass());
//        return R.error(HttpCode.UNKNOW_EXCEPTION.getCode(), HttpCode.UNKNOW_EXCEPTION.getMessage());
//    }

}
