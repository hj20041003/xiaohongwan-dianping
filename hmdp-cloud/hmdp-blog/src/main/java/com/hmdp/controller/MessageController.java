package com.hmdp.controller;

import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Message;
import com.hmdp.service.IMessageService;
import com.hmdp.utils.UserHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 消息中心：列表 / 未读数 / 全部已读
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private IMessageService messageService;

    private Result requireLogin(HttpServletResponse response) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            response.setStatus(401);
            return Result.fail("请先登录");
        }
        return null;
    }

    /**
     * 我的消息列表（最新 50 条）
     */
    @GetMapping("/list")
    public Result list(HttpServletResponse response) {
        Result err = requireLogin(response);
        if (err != null) return err;
        UserDTO user = UserHolder.getUser();
        List<Message> list = messageService.query()
                .eq("user_id", user.getId())
                .orderByDesc("create_time")
                .last("LIMIT 50")
                .list();
        return Result.ok(list);
    }

    /**
     * 未读消息数
     */
    @GetMapping("/unread")
    public Result unread(HttpServletResponse response) {
        Result err = requireLogin(response);
        if (err != null) return err;
        UserDTO user = UserHolder.getUser();
        int count = messageService.query()
                .eq("user_id", user.getId())
                .eq("is_read", 0)
                .count();
        return Result.ok(count);
    }

    /**
     * 全部标记已读
     */
    @PostMapping("/read")
    public Result readAll(HttpServletResponse response) {
        Result err = requireLogin(response);
        if (err != null) return err;
        UserDTO user = UserHolder.getUser();
        messageService.update()
                .eq("user_id", user.getId())
                .eq("is_read", 0)
                .set("is_read", 1)
                .update();
        return Result.ok();
    }
}
