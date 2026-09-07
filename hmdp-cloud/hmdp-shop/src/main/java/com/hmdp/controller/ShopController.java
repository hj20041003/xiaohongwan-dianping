package com.hmdp.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import com.hmdp.utils.SystemConstants;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制�?
 * </p>
 *
 * @author liangzhican
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    public IShopService shopService;

    /**
     * 根据id查询商铺信息
     * @param id 商铺id
     * @return 商铺详情数据
     */
    @GetMapping("/{id}")
    public Result queryShopById(@PathVariable("id") Long id) {
        return Result.ok(shopService.getById(id));
    }

    /**
     * 新增商铺信息
     * @param shop 商铺数据
     * @return 商铺id
     */
    @PostMapping
    public Result saveShop(@RequestBody Shop shop) {
        // 写入数据�?
        shopService.save(shop);
        // 返回店铺id
        return Result.ok(shop.getId());
    }

    /**
     * 更新商铺信息
     * @param shop 商铺数据
     * @return �?
     */
    @PutMapping
    public Result updateShop(@RequestBody Shop shop) {
        // 写入数据�?
        shopService.updateById(shop);
        return Result.ok();
    }

    /**
     * 根据商铺类型分页查询商铺信息
     * @param typeId 商铺类型
     * @param current 页码
     * @param sortBy 排序方式：空=默认, score=评分, comments=人气, distance=距离(需传x/y)
     * @param x 用户经度（距离排序用）
     * @param y 用户纬度（距离排序用）
     * @return 商铺列表
     */
    @GetMapping("/of/type")
    public Result queryShopByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "sortBy", required = false, defaultValue = "") String sortBy,
            @RequestParam(value = "x", required = false) Double x,
            @RequestParam(value = "y", required = false) Double y
    ) {
        // 根据类型分页查询 + 可选排序
        com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<Shop> query =
                shopService.lambdaQuery().eq(Shop::getTypeId, typeId);
        if ("score".equals(sortBy)) {
            query.orderByDesc(Shop::getScore);
        } else if ("comments".equals(sortBy)) {
            query.orderByDesc(Shop::getComments);
        } else if ("distance".equals(sortBy) && x != null && y != null) {
            // 小范围内平面距离近似排序（经纬度平方和），数值参数无注入风险
            query.last(String.format(
                    "ORDER BY ((x - %.8f) * (x - %.8f) + (y - %.8f) * (y - %.8f)) ASC", x, x, y, y));
        }
        Page<Shop> page = query.page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
        // 返回数据
        return Result.ok(page.getRecords());
    }

    /**
     * 根据商铺名称关键字分页查询商铺信�?
     * @param name 商铺名称关键�?
     * @param current 页码
     * @return 商铺列表
     */
    @GetMapping("/of/name")
    public Result queryShopByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        Page<Shop> page = shopService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 返回数据
        return Result.ok(page.getRecords());
    }

    /**
     * 查询热门商铺
     * @param current 页码
     * @return 热门商铺列表
     */
    @GetMapping("/hot")
    public Result queryHotShop(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 根据评分查询热门商铺
        Page<Shop> page = shopService.query()
                .orderByDesc("score")
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
        // 返回数据
        return Result.ok(page.getRecords());
    }

    /**
     * 管理后端：全部商户列表
     */
    @GetMapping("/list")
    public Result listAll() {
        return Result.ok(shopService.query().orderByAsc("id").list());
    }

    /**
     * 地图找店：全部商户的坐标与基本信息
     */
    @GetMapping("/geo/list")
    public Result geoList() {
        return Result.ok(shopService.query()
                .select("id", "name", "x", "y", "address", "avg_price", "score")
                .list());
    }

    /**
     * 管理后端：删除商户
     */
    @DeleteMapping("/{id}")
    public Result deleteShop(@PathVariable("id") Long id) {
        shopService.removeById(id);
        return Result.ok();
    }

    /**
     * 关键字搜索商户：匹配名称或地址（用于顶部搜索栏 / 地图地址搜索）
     * @param keyword 关键字
     * @param current 页码
     * @return 商户列表
     */
    @GetMapping("/search")
    public Result searchShop(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        Page<Shop> page = shopService.lambdaQuery()
                .and(StrUtil.isNotBlank(keyword), w -> w
                        .like(Shop::getName, keyword)
                        .or().like(Shop::getAddress, keyword)
                        .or().like(Shop::getArea, keyword))
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        return Result.ok(page.getRecords());
    }
}
