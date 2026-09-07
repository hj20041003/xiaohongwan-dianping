package com.hmdp.controller;

import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.UserFavorite;
import com.hmdp.service.IShopService;
import com.hmdp.service.IUserFavoriteService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏/心愿单 —— 新增功能
 */
@RestController
@RequestMapping("/favorite")
public class UserFavoriteController {

    @Resource
    private IUserFavoriteService favoriteService;

    @Resource
    private IShopService shopService;

    /**
     * 收藏/取消收藏（切换）
     */
    @PostMapping("/toggle")
    public Result toggle(@RequestParam("userId") Long userId,
                         @RequestParam("shopId") Long shopId) {
        UserFavorite exist = favoriteService.query()
                .eq("user_id", userId).eq("shop_id", shopId).one();
        if (exist != null) {
            favoriteService.removeById(exist.getId());
            Map<String, Object> data = new HashMap<>();
            data.put("favorite", false);
            return Result.ok(data);
        }
        UserFavorite fav = new UserFavorite()
                .setUserId(userId).setShopId(shopId)
                .setCreateTime(LocalDateTime.now());
        favoriteService.save(fav);
        Map<String, Object> data = new HashMap<>();
        data.put("favorite", true);
        return Result.ok(data);
    }

    /**
     * 我的收藏列表（返回商户详情）
     */
    @GetMapping("/list")
    public Result list(@RequestParam("userId") Long userId) {
        List<UserFavorite> favs = favoriteService.query()
                .eq("user_id", userId).orderByDesc("create_time").list();
        List<Shop> shops = new ArrayList<>();
        for (UserFavorite f : favs) {
            Shop shop = shopService.getById(f.getShopId());
            if (shop != null) {
                shops.add(shop);
            }
        }
        return Result.ok(shops);
    }

    /**
     * 检查某用户是否已收藏某商户
     */
    @GetMapping("/check")
    public Result check(@RequestParam("userId") Long userId,
                        @RequestParam("shopId") Long shopId) {
        int count = favoriteService.query()
                .eq("user_id", userId).eq("shop_id", shopId).count();
        Map<String, Object> data = new HashMap<>();
        data.put("favorite", count > 0);
        return Result.ok(data);
    }
}
