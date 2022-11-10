package com.atguigu.gmall.user.client.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.client.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user/passport")
public class PassPortApiController {
    @Resource
    private UserInfoService userInfoService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * /api/user/passport/login
     * 用户登录功能
     *
     * @return
     */
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo,
                        HttpServletRequest request,
                        HttpServletResponse response) {
        UserInfo info = userInfoService.login(userInfo);
        if (info != null) {
            String token = UUID.randomUUID().toString().replace("-", "");
            Map<String, Object> map = new HashMap<>();
            map.put("nickName", info.getNickName());
            map.put("token", token);

            //在redis缓存中存储用户信息
            JSONObject userJson = new JSONObject();
            userJson.put("userId", info.getId().toString());
            userJson.put("ip", IpUtil.getIpAddress(request));
            redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + token,
                    userJson.toJSONString(), RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            return Result.ok(map);
        } else {
            return Result.fail("用户或密码错误");
        }
    }

    @GetMapping("logout")
    public Result logout(HttpServletRequest request) {
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX +
                request.getHeader("token"));

        return Result.ok();
    }
}
