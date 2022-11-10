package com.atguigu.gmall.user.client.service;

import com.atguigu.gmall.model.user.UserAddress;

import java.util.List;

public interface UserAddressService {
    //根据用户Id 查询用户的收货地址列表！
    List<UserAddress> findUserAddressListByUserId(Long userId);
}
