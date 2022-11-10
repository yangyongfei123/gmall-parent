package com.atguigu.gmall.user.client.controller;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.client.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserApiController {
    @Autowired
    private UserAddressService userAddressService;

    /**
     * 根据用户Id 查询用户的收货地址列表！
     * /api/user/inner/findUserAddressListByUserId/{userId}
     * @param userId
     * @return
     */

    @GetMapping("inner/findUserAddressListByUserId/{userId}")
    public List<UserAddress> findUserAddressListByUserId(@PathVariable Long userId) {
        List<UserAddress> userAddressList = userAddressService.findUserAddressListByUserId(userId);
        return userAddressList;
    }
}
