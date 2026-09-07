package com.hmdp.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.client.UserClient;
import com.hmdp.service.IBlogService;
import com.hmdp.entity.Message;
import com.hmdp.service.IMessageService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 前端控制�?
 * </p>
 *
 * @author liangzhican
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService;
    @Resource
    private UserClient userClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IMessageService messageService;

    /***
     * @description: TODO :保存博客到数据库并推送给粉丝
     * @params: [blog]
     * @return: com.hmdp.dto.Result
     * @author: SenGang
     */

    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {

        // 返回id
        return blogService.saveBlog(blog);
    }

    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, user.getId().toString());
        if (score == null) {
            // 未点赞：点赞数+1，记录到 zset
            blogService.update()
                    .setSql("liked = liked + 1").eq("id", id).update();
            stringRedisTemplate.opsForZSet().add(key, user.getId().toString(), System.currentTimeMillis());
            // 给作者发消息
            Blog blog = blogService.getById(id);
            if (blog != null && !user.getId().equals(blog.getUserId())) {
                Message msg = new Message()
                        .setUserId(blog.getUserId())
                        .setType(1)
                        .setContent(user.getNickName() + " 赞了你的笔记")
                        .setTargetId(id)
                        .setIsRead(0);
                messageService.save(msg);
            }
        } else {
            // 已点赞：取消点赞
            blogService.update()
                    .setSql("liked = liked - 1").eq("id", id).update();
            stringRedisTemplate.opsForZSet().remove(key, user.getId().toString());
        }
        return Result.ok();
    }

    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 使用当前登录用户（未登录回退为 1）
        com.hmdp.dto.UserDTO me = UserHolder.getUser();
        Long userId = me != null ? me.getId() : 1L;
        Page<Blog> page = blogService.query()
                .eq("user_id", userId).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数�?
        List<Blog> records = page.getRecords();
        // 查询用户信息
        fillBlogger(records);
        return Result.ok(records);
    }

    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数�?
        List<Blog> records = page.getRecords();
        // 查询用户
        fillBlogger(records);
        return Result.ok(records);
    }

    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable("id") Long id) {
        // 根据id查询博客
        Blog blog = blogService.getById(id);
        if (blog == null) {
            return Result.fail("博客不存在");
        }
        // 查询博客相关的用�?
        fillBlogger(java.util.Collections.singletonList(blog));
        return Result.ok(blog);
    }

    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable("id") Long id) {
        // 查询点赞用户列表（这里简化处理，返回空列表）
        return Result.ok(java.util.Collections.emptyList());
    }

    @GetMapping("/of/follow")
    public Result queryBlogOfFollow(@RequestParam("lastId") Long max, 
                                   @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        // 查询关注的人的博客（简化处理，返回热门博客�?
        Page<Blog> page = blogService.query()
                .orderByDesc("liked")
                .page(new Page<>(1, 5));
        List<Blog> records = page.getRecords();
        // 查询用户信息
        fillBlogger(records);
        
        // 构造滚动分页返回格�?
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("list", records);
        result.put("minTime", System.currentTimeMillis());
        result.put("offset", 0);
        return Result.ok(result);
    }

    /**
     * 管理后端：全部笔记列表
     */
    @GetMapping("/list")
    public Result listAll() {
        List<Blog> records = blogService.query().orderByDesc("liked").list();
        fillBlogger(records);
        return Result.ok(records);
    }

    /**
     * 管理后端：删除笔记
     */
    @DeleteMapping("/{id}")
    public Result deleteBlog(@PathVariable("id") Long id) {
        blogService.removeById(id);
        stringRedisTemplate.delete(RedisConstants.BLOG_LIKED_KEY + id);
        return Result.ok();
    }

    /**
     * 批量填充笔记作者信息（通过 Feign 调用 user 服务）
     */
    private void fillBlogger(List<Blog> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> ids = records.stream().map(Blog::getUserId)
                .filter(Objects::nonNull).distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, UserDTO> users = userClient.batchByIds(ids).stream()
                .collect(Collectors.toMap(UserDTO::getId, Function.identity()));
        records.forEach(blog -> {
            UserDTO u = users.get(blog.getUserId());
            if (u != null) {
                blog.setName(u.getNickName());
                blog.setIcon(u.getIcon());
            }
        });
    }
}
