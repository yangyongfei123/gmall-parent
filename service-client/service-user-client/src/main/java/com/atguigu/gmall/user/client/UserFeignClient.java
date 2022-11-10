package com.atguigu.gmall.user.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.client.impl.UserDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@FeignClient(value = "service-user",fallback = UserDegradeFeignClient.class)
public interface UserFeignClient {
    /**
     * /api/user/passport/login
     * 用户登录功能
     * @return
     */
    @PostMapping("/api/user/passport/login")
    public Result login(@RequestBody UserInfo userInfo,
                        HttpServletRequest request,
                        HttpServletResponse response);

    /**
     * /api/user/passport/login
     * 用户退出功能
     * @return
     */
    @GetMapping("/api/user/passport/logout")
    public Result logout(HttpServletRequest request);

    /**
     * 根据用户Id 查询用户的收货地址列表！
     * /api/user/inner/findUserAddressListByUserId/{userId}
     * @param userId
     * @return
     */
    @GetMapping("/api/user/inner/findUserAddressListByUserId/{userId}")
    public List<UserAddress> findUserAddressListByUserId(@PathVariable Long userId);
}
