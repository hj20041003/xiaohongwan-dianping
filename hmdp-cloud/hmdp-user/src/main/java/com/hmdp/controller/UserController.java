package com.hmdp.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;
import com.hmdp.service.IUserInfoService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务：验证码 / 登录 / 登出 / 当前用户 / 用户列表
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送手机验证码（Mock：直接返回验证码便于演示）
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        if (!RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(
                RedisConstants.LOGIN_CODE_KEY + phone, code, 2, TimeUnit.MINUTES);
        log.info("发送短信验证码: phone={} code={}", phone, code);
        // 演示环境直接回显验证码
        return Result.ok(code);
    }

    /**
     * 登录：校验验证码，新用户自动注册，返回 token
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        if (!RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        String cacheCode = stringRedisTemplate.opsForValue()
                .get(RedisConstants.LOGIN_CODE_KEY + phone);
        if (cacheCode == null || !cacheCode.equals(loginForm.getCode())) {
            return Result.fail("验证码错误或已过期");
        }
        stringRedisTemplate.delete(RedisConstants.LOGIN_CODE_KEY + phone);

        // 查询或注册用户
        User user = userService.query().eq("phone", phone).one();
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setNickName(SystemConstants_USER_NICK_PREFIX + RandomUtil.randomString(8));
            user.setIcon("/imgs/icons/default-icon.png");
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userService.save(user);
        }

        // 生成 token 并保存会话
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setNickName(user.getNickName());
        dto.setIcon(user.getIcon());
        String token = UUID.randomUUID().toString().replace("-", "");
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForValue().set(tokenKey, JSONUtil.toJsonStr(dto),
                RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        return Result.ok(token);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        String token = request.getHeader("authorization");
        if (token != null && !token.isEmpty()) {
            stringRedisTemplate.delete(RedisConstants.LOGIN_USER_KEY + token);
        }
        return Result.ok();
    }

    /**
     * 当前登录用户（未登录返回 401，前端会跳转登录页）
     */
    @GetMapping("/me")
    public Result me(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader("authorization");
        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            return Result.fail("请先登录");
        }
        String json = stringRedisTemplate.opsForValue()
                .get(RedisConstants.LOGIN_USER_KEY + token);
        if (json == null) {
            response.setStatus(401);
            return Result.fail("登录已过期，请重新登录");
        }
        return Result.ok(JSONUtil.toBean(json, UserDTO.class));
    }

    /**
     * 内部接口：批量查询用户（供 blog 服务通过 Feign 调用）
     */
    @GetMapping("/batch")
    public List<UserDTO> batchByIds(@RequestParam("ids") List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<UserDTO> result = new ArrayList<>();
        userService.listByIds(ids).forEach(u -> {
            UserDTO dto = new UserDTO();
            dto.setId(u.getId());
            dto.setNickName(u.getNickName());
            dto.setIcon(u.getIcon());
            result.add(dto);
        });
        return result;
    }

    /**
     * 修改昵称（以登录 token 定位当前用户，并同步更新 Redis 会话）
     */
    @PutMapping("/nickName")
    public Result updateNickName(@RequestParam("nickName") String nickName, HttpServletRequest request) {
        UserDTO me = currentUser(request);
        if (me == null) {
            return Result.fail("请先登录");
        }
        nickName = nickName == null ? "" : nickName.trim();
        if (nickName.isEmpty() || nickName.length() > 16) {
            return Result.fail("昵称需为 1-16 个字符");
        }
        User user = userService.getById(me.getId());
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setNickName(nickName);
        userService.updateById(user);
        // 同步 Redis 会话
        me.setNickName(nickName);
        String token = request.getHeader("authorization");
        if (token != null && !token.isEmpty()) {
            stringRedisTemplate.opsForValue().set(
                    RedisConstants.LOGIN_USER_KEY + token, JSONUtil.toJsonStr(me),
                    RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        }
        return Result.ok(me);
    }

    /**
     * 修改头像（前端先经 /upload/blog 上传拿到路径，再调本接口写入用户与 Redis 会话）
     */
    @PutMapping("/icon")
    public Result updateIcon(@RequestParam("icon") String icon, HttpServletRequest request) {
        UserDTO me = currentUser(request);
        if (me == null) {
            return Result.fail("请先登录");
        }
        icon = icon == null ? "" : icon.trim();
        if (icon.isEmpty() || icon.length() > 200) {
            return Result.fail("头像路径不合法");
        }
        User user = userService.getById(me.getId());
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setIcon(icon);
        userService.updateById(user);
        me.setIcon(icon);
        String token = request.getHeader("authorization");
        if (token != null && !token.isEmpty()) {
            stringRedisTemplate.opsForValue().set(
                    RedisConstants.LOGIN_USER_KEY + token, JSONUtil.toJsonStr(me),
                    RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        }
        return Result.ok(me);
    }

    /**
     * 保存个人资料（介绍/性别/城市/生日），以登录用户 id 写 tb_user_info
     */
    @PostMapping("/info/update")
    public Result updateInfo(@RequestBody UserInfo info, HttpServletRequest request) {
        UserDTO me = currentUser(request);
        if (me == null) {
            return Result.fail("请先登录");
        }
        info.setUserId(me.getId());
        UserInfo exist = userInfoService.getById(me.getId());
        if (exist == null) {
            userInfoService.save(info);
        } else {
            userInfoService.updateById(info);
        }
        return Result.ok();
    }

    private UserDTO currentUser(HttpServletRequest request) {
        String token = request.getHeader("authorization");
        if (token == null || token.isEmpty()) {
            return null;
        }
        String json = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_USER_KEY + token);
        return json == null ? null : JSONUtil.toBean(json, UserDTO.class);
    }

    /**
     * 管理后端：用户列表
     */
    @GetMapping("/list")
    public Result listAll() {
        List<User> users = userService.query().orderByAsc("id").list();
        users.forEach(u -> u.setPassword(null));
        return Result.ok(users);
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId) {
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        return Result.ok(info);
    }

    private static final String SystemConstants_USER_NICK_PREFIX = "user_";
}
