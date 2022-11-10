package com.atguigu.gmall.user.client.service.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.client.mapper.UserAddressServiceMapper;
import com.atguigu.gmall.user.client.service.UserAddressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserAddressServiceImpl implements UserAddressService {
    @Resource
    private UserAddressServiceMapper userAddressServiceMapper;

    //根据用户Id 查询用户的收货地址列表！
    @Override
    public List<UserAddress> findUserAddressListByUserId(Long userId) {
        QueryWrapper<UserAddress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<UserAddress> userAddressList = userAddressServiceMapper.selectList(queryWrapper);
        return userAddressList;
    }
}
