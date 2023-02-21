package com.xha.gulimall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class SentinelGatewayConfig {

    public SentinelGatewayConfig() {
        GatewayCallbackManager.setBlockHandler(
                (serverWebExchange, throwable) -> {
                    String response = "{\n" +
                            "\"code\":400,\n" +
                            "\"message\":\"请求过于频繁，请稍后重试\"\n" +
                            "}";
                    Mono<ServerResponse> monoResult = ServerResponse.ok().body(Mono.just(response), String.class);
                    return monoResult;
                }
        );
    }
}
