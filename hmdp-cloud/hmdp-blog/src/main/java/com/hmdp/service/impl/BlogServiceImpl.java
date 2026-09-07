package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IFollowService;
import com.hmdp.utils.UserHolder;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.hmdp.utils.RedisConstants.FEED_KEY;

/**
 * <p>
 *  服务实现�?
 * </p>
 *
 * @author liangzhican
 * @since 2021-12-22
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private IFollowService followService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result saveBlog(Blog blog) {

        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user != null ? user.getId() : 1L);
        // 兜底默认值，避免前端渲染空指针
        if (blog.getImages() == null || blog.getImages().trim().isEmpty()) {
            blog.setImages("/imgs/icons/default-icon.png");
        }
        if (blog.getComments() == null) {
            blog.setComments(0);
        }
        if (blog.getLiked() == null) {
            blog.setLiked(0);
        }
        if (blog.getShopId() == null) {
            blog.setShopId(1L);
        }
        if (blog.getTitle() == null || blog.getTitle().trim().isEmpty()) {
            blog.setTitle("分享一篇探店笔记");
        }
        // 保存探店博文
        boolean isSave = save(blog);
        if (!isSave)
            return Result.fail("笔记保存失败");
        //查询作者的所有粉�?
        List<Follow> followList = followService.query().eq("follow_user_id", blog.getUserId()).list();
        //推送id给所有粉�?
        for (Follow follow :followList) {
            //获取粉丝id
            Long userId = follow.getUserId();
            String key = FEED_KEY+ userId;
            //添加blogid到粉丝收件箱，zset
            stringRedisTemplate.opsForZSet().add(key,blog.getId().toString(),System.currentTimeMillis());
        }

        // 返回id
        return Result.ok(blog.getId());
    }
}
