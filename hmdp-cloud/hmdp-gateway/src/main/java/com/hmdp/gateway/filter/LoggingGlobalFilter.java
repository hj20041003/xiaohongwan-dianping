package com.hmdp.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局日志过滤器：记录每个经过网关的请求（路由目标、耗时）
 */
@Slf4j
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        String path = exchange.getRequest().getPath().value();
        return chain.filter(exchange).then(Mono.fromRunnable(() ->
                log.info("[gateway] {} {} -> {}ms", exchange.getRequest().getMethod(), path,
                        System.currentTimeMillis() - start)));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
