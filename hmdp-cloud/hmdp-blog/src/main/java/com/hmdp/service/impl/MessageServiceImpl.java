package com.hmdp.service.impl;

import com.hmdp.entity.Message;
import com.hmdp.mapper.MessageMapper;
import com.hmdp.service.IMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * Message 服务实现
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

}
