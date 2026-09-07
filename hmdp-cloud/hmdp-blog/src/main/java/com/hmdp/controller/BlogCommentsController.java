package com.hmdp.controller;

import cn.hutool.core.util.StrUtil;
import com.hmdp.client.UserClient;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.BlogComments;
import com.hmdp.entity.Message;
import com.hmdp.service.IBlogCommentsService;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IMessageService;
import com.hmdp.utils.UserHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 笔记评论：发表 / 列表（带用户信息）
 */
@RestController
@RequestMapping("/blog-comments")
public class BlogCommentsController {

    @Resource
    private IBlogCommentsService blogCommentsService;

    @Resource
    private IBlogService blogService;

    @Resource
    private IMessageService messageService;

    @Resource
    private UserClient userClient;

    /**
     * 发表评论（需登录），评论数 +1 并通知作者
     */
    @PostMapping
    public Result addComment(@RequestBody BlogComments comment) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        if (comment.getBlogId() == null || StrUtil.isBlank(comment.getContent())) {
            return Result.fail("参数不完整");
        }
        String content = comment.getContent().trim();
        if (content.isEmpty() || content.length() > 200) {
            return Result.fail("评论内容需为 1-200 字");
        }
        comment.setUserId(user.getId());
        comment.setParentId(0L);
        comment.setAnswerId(0L);
        comment.setLiked(0);
        comment.setStatus(true);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        blogCommentsService.save(comment);

        // 笔记评论数 +1
        blogService.update()
                .setSql("comments = comments + 1")
                .eq("id", comment.getBlogId())
                .update();

        // 通知作者
        Blog blog = blogService.getById(comment.getBlogId());
        if (blog != null && !user.getId().equals(blog.getUserId())) {
            Message msg = new Message()
                    .setUserId(blog.getUserId())
                    .setType(1)
                    .setContent(user.getNickName() + " 评论了你的笔记")
                    .setTargetId(comment.getBlogId())
                    .setIsRead(0);
            messageService.save(msg);
        }
        return Result.ok();
    }

    /**
     * 笔记评论列表（含评论人昵称/头像）
     */
    @GetMapping("/list")
    public Result list(@RequestParam("blogId") Long blogId) {
        List<BlogComments> list = blogCommentsService.query()
                .eq("blog_id", blogId)
                .orderByDesc("create_time")
                .list();
        if (list.isEmpty()) {
            return Result.ok(new ArrayList<>());
        }
        // 批量取评论人信息（Feign → user 服务）
        List<Long> userIds = list.stream()
                .map(BlogComments::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, UserDTO> users = new HashMap<>();
        if (!userIds.isEmpty()) {
            try {
                users = userClient.batchByIds(userIds).stream()
                        .collect(Collectors.toMap(UserDTO::getId, Function.identity()));
            } catch (Exception ignore) {
                // 用户服务不可用时降级为匿名显示
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (BlogComments c : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("blogId", c.getBlogId());
            item.put("userId", c.getUserId());
            item.put("content", c.getContent());
            item.put("createTime", c.getCreateTime());
            UserDTO u = users.get(c.getUserId());
            item.put("nickName", u != null ? u.getNickName() : "小红碗用户");
            item.put("icon", u != null ? u.getIcon() : "/imgs/icons/default-icon.png");
            result.add(item);
        }
        return Result.ok(result);
    }
}
