package com.hmdp.service.impl;

import com.hmdp.entity.UserFavorite;
import com.hmdp.mapper.UserFavoriteMapper;
import com.hmdp.service.IUserFavoriteService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * UserFavorite 服务实现
 */
@Service
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements IUserFavoriteService {

}
