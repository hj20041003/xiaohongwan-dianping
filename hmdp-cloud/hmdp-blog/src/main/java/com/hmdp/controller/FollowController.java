package com.hmdp.controller;


import com.hmdp.dto.Result;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制�?
 * </p>
 *
 * @author liangzhican
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/follow")
public class FollowController {

    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") Long followUserId) {
        // 简化处理，返回false表示未关�?
        return Result.ok(false);
    }

    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") Boolean isFollow) {
        // 简化处理，直接返回成功
        return Result.ok();
    }
}
