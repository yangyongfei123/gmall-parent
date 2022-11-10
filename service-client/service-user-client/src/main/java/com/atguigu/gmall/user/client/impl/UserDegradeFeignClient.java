package com.atguigu.gmall.user.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Component
public class UserDegradeFeignClient implements UserFeignClient {
    @Override
    public Result login(UserInfo userInfo, HttpServletRequest request, HttpServletResponse response) {
        return null;
    }
    @Override
    public Result logout(HttpServletRequest request) {
        return null;
    }
    @Override
    public List<UserAddress> findUserAddressListByUserId(Long userId) {
        return null;
    }
}
