package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商户细分评分（口味/环境/服务）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_shop_score")
public class ShopScore implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商户 id
     */
    private Long shopId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 口味评分 1-5
     */
    private Integer taste;

    /**
     * 环境评分 1-5
     */
    private Integer environment;

    /**
     * 服务评分 1-5
     */
    private Integer service;

    /**
     * 评语
     */
    private String content;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
