package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.service.CartApiService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.aspectj.weaver.patterns.Declare;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartApiServiceImpl implements CartApiService {
    @Resource
    private ProductFeignClient productFeignClient;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void addToCart(Long skuId, String userId, Integer skuNum) {
        //  获取缓存key
        String cartKey = getCartKey(userId);

        BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(cartKey);

        //包含的话更新数量
        if (boundHashOps.hasKey(skuId.toString())) {
            CartInfo cartInfo = boundHashOps.get(skuId.toString());
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            cartInfo.setIsChecked(1);
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(skuId));
            cartInfo.setUpdateTime(new Date());
        } else {
            //  给cartInfo 赋值！
            CartInfo cartInfo = new CartInfo();
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            cartInfo.setUserId(userId);
            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());
            cartInfo.setCartPrice(skuInfo.getPrice());
        }
    }

    @Override
    public List<CartInfo> getCartList(String userId, String userTempId) {
        List<CartInfo> cartInfoList = null;
        //  声明一个集合来存储未登录数据
        List<CartInfo> noLoginCartInfoList = null;

        //  ------------------- 属于未登录,没有userId ,有userTempId   return noLoginCartInfoList------------
        if (!StringUtils.isEmpty(userTempId)) {
            //  获取登录的购物车集合数据！
            String cartKey = getCartKey(userTempId);
            noLoginCartInfoList = redisTemplate.opsForHash().values(cartKey);
        }
        //  这个是集合的排序
        if (StringUtils.isEmpty(userId)) {
            if (!CollectionUtils.isEmpty(noLoginCartInfoList)) {
                noLoginCartInfoList.sort((o1, o2) -> {
                    return DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND);
                });
            }
            //  返回未登录数据！
            return noLoginCartInfoList;
        }
        //  -------------------------------- 属于登录-----------------------------------------------

        //  先获取到登录购物车的key

        List<CartInfo> loginCartInfoList = null;
        String cartKey = getCartKey(userId);
        //  合并思路二：
        BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(cartKey);
        //  判断购物车中的field
        if (!CollectionUtils.isEmpty(noLoginCartInfoList)) {

            //  循环遍历未登录购物车集合
            noLoginCartInfoList.stream().forEach(cartInfo -> {
                CartInfo loginCartInfo = null;
                //  合并业务逻辑 : skuNum + skuNum 更新时间
                if (boundHashOps.hasKey(cartInfo.getSkuId().toString())) {
                    loginCartInfo = boundHashOps.get(cartInfo.getSkuId().toString());

                    loginCartInfo.setSkuNum(loginCartInfo.getSkuNum() + cartInfo.getSkuNum());
                    loginCartInfo.setUpdateTime(new Date());
                    loginCartInfo.setSkuPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));

                    //  选中状态合并！
                    if (cartInfo.getIsChecked() == 1) {
                        loginCartInfo.setIsChecked(1);
                    }
                    boundHashOps.put(cartInfo.getSkuId().toString(), loginCartInfo);
                } else {
                    //  直接添加到缓存！
                    cartInfo.setUserId(userId);
                    cartInfo.setCreateTime(new Date());
                    cartInfo.setUpdateTime(new Date());

                    boundHashOps.put(cartInfo.getSkuId().toString(), cartInfo);
                }
            });

            //  删除未登录购物车数据！
            redisTemplate.delete(getCartKey(userTempId));
        }
        //  获取到合并之后的数据：
        loginCartInfoList = redisTemplate.boundHashOps(cartKey).values();

        if (CollectionUtils.isEmpty(loginCartInfoList)) {
            return new ArrayList<>();
        }
        //  排序
        loginCartInfoList.sort((o1, o2) -> {
            return DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND);
        });
        return loginCartInfoList;
    }

    @Override
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        String cartKey = getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(cartKey);
        CartInfo cartInfo = boundHashOps.get(skuId.toString());
        if (cartInfo != null) {
            cartInfo.setIsChecked(isChecked);
            boundHashOps.put(skuId.toString(), cartInfo);
        }
    }

    @Override
    public void deleteCart(String userId, Long skuId) {
        BoundHashOperations<String, String, CartInfo> boundHashOps =
                redisTemplate.boundHashOps(getCartKey(userId));
        //  判断购物车中是否有该商品！
        if (boundHashOps.hasKey(skuId.toString())) {
            redisTemplate.delete(skuId.toString());
        }
    }

    @Override
    public List<CartInfo> getCartCheckedList(Long userId) {
        //  获取的选中的购物车列表的key！
        String cartKey = getCartKey(String.valueOf(userId));
        //  获取到购物车集合数据：
        List<CartInfo> cartInfoList = redisTemplate.opsForHash().values(cartKey);
        //  再次确认一下  最新价格
        List<CartInfo> cartInfos = cartInfoList.stream().filter(cartInfo -> {
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
            return cartInfo.getIsChecked().intValue() == 1;
        }).collect(Collectors.toList());

        return cartInfos;
    }

    private String getCartKey(String userId) {
        String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
        return cartKey;
    }
}
