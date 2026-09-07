package com.hmdp.controller;

import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopScore;
import com.hmdp.service.IShopScoreService;
import com.hmdp.service.IShopService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户细分评分（口味/环境/服务）——新增功能
 */
@RestController
@RequestMapping("/score")
public class ShopScoreController {

    @Resource
    private IShopScoreService scoreService;

    @Resource
    private IShopService shopService;

    /**
     * 提交/更新评分（一个用户对一家店只有一条记录，重复提交则覆盖更新）
     */
    @PostMapping
    public Result submitScore(@RequestParam("shopId") Long shopId,
                              @RequestParam("userId") Long userId,
                              @RequestParam("taste") Integer taste,
                              @RequestParam("environment") Integer environment,
                              @RequestParam("service") Integer service,
                              @RequestParam(value = "content", required = false) String content) {
        if (taste == null || taste < 1 || taste > 5
                || environment == null || environment < 1 || environment > 5
                || service == null || service < 1 || service > 5) {
            return Result.fail("评分必须在 1-5 之间");
        }
        if (shopId == null || userId == null) {
            return Result.fail("shopId 和 userId 不能为空");
        }
        ShopScore exist = scoreService.query()
                .eq("shop_id", shopId).eq("user_id", userId).one();
        LocalDateTime now = LocalDateTime.now();
        if (exist != null) {
            exist.setTaste(taste).setEnvironment(environment).setService(service)
                    .setContent(content).setUpdateTime(now);
            scoreService.updateById(exist);
            return Result.ok(exist);
        }
        ShopScore score = new ShopScore()
                .setShopId(shopId).setUserId(userId)
                .setTaste(taste).setEnvironment(environment).setService(service)
                .setContent(content).setCreateTime(now).setUpdateTime(now);
        scoreService.save(score);
        return Result.ok(score);
    }

    /**
     * 查询某商户的评分聚合 + 明细
     */
    @GetMapping("/detail")
    public Result detail(@RequestParam("shopId") Long shopId) {
        List<ShopScore> scores = scoreService.query().eq("shop_id", shopId).list();
        Map<String, Object> data = aggregate(scores);
        data.put("scores", scores);
        return Result.ok(data);
    }

    /**
     * 所有商户的评分聚合排名（按综合分降序）
     */
    @GetMapping("/rank")
    public Result rank() {
        List<ShopScore> all = scoreService.list();
        // 按 shopId 分组聚合
        Map<Long, List<ShopScore>> byShop = new HashMap<>();
        for (ShopScore s : all) {
            byShop.computeIfAbsent(s.getShopId(), k -> new ArrayList<>()).add(s);
        }
        List<Map<String, Object>> rankList = new ArrayList<>();
        for (Map.Entry<Long, List<ShopScore>> e : byShop.entrySet()) {
            Map<String, Object> agg = aggregate(e.getValue());
            agg.put("shopId", e.getKey());
            Shop shop = shopService.getById(e.getKey());
            agg.put("shopName", shop != null ? shop.getName() : "未知商户");
            rankList.add(agg);
        }
        rankList.sort((a, b) -> Double.compare(
                (Double) b.get("overall"), (Double) a.get("overall")));
        return Result.ok(rankList);
    }

    /**
     * 聚合计算：口味/环境/服务平均分 + 综合 = (口味+环境+服务)/3
     */
    private Map<String, Object> aggregate(List<ShopScore> scores) {
        Map<String, Object> data = new HashMap<>();
        int n = scores.size();
        if (n == 0) {
            data.put("count", 0);
            data.put("taste", 0.0);
            data.put("environment", 0.0);
            data.put("service", 0.0);
            data.put("overall", 0.0);
            return data;
        }
        double taste = 0, env = 0, svc = 0;
        for (ShopScore s : scores) {
            taste += s.getTaste();
            env += s.getEnvironment();
            svc += s.getService();
        }
        taste /= n; env /= n; svc /= n;
        data.put("count", n);
        data.put("taste", Math.round(taste * 10) / 10.0);
        data.put("environment", Math.round(env * 10) / 10.0);
        data.put("service", Math.round(svc * 10) / 10.0);
        data.put("overall", Math.round((taste + env + svc) / 3 * 10) / 10.0);
        return data;
    }
}
