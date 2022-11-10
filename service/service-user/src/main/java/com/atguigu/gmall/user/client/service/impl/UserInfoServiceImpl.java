package com.atguigu.gmall.user.client.service.impl;

import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.client.service.UserInfoService;
import com.atguigu.gmall.user.client.mapper.UserInfoServiceMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Resource
    private UserInfoServiceMapper userInfoServiceMapper;

    @Override
    public UserInfo login(UserInfo userInfo) {
        // 注意密码是加密的：
        String passwd = userInfo.getPasswd();
        // 将passwd 进行加密
        String newPasswd  = DigestUtils.md5DigestAsHex(passwd.getBytes());


        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_name", userInfo.getLoginName());
        queryWrapper.eq("passwd", newPasswd);
        UserInfo userInfo1 = userInfoServiceMapper.selectOne(queryWrapper);
        if (userInfo1 != null) {
            return userInfo1;
        }
        return null;
    }
}
