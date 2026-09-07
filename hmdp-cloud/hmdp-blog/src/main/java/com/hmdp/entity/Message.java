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
 * 消息（点赞通知 / 系统通知）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_message")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收消息的用户
     */
    private Long userId;

    /**
     * 类型：1=点赞 2=系统
     */
    private Integer type;

    private String content;

    /**
     * 关联对象 id（如笔记 id）
     */
    private Long targetId;

    /**
     * 0=未读 1=已读
     */
    private Integer isRead;

    private LocalDateTime createTime;
}
