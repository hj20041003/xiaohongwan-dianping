package com.hmdp.client;

import com.hmdp.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 用户服务远程调用客户端（Feign + Nacos 负载均衡）
 */
@FeignClient("hmdp-user")
public interface UserClient {

    @GetMapping("/user/batch")
    List<UserDTO> batchByIds(@RequestParam("ids") List<Long> ids);
}
