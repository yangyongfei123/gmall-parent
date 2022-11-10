package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("/api/order")
public class OrderApiController {
    @Resource
    private UserFeignClient userFeignClient;
    @Resource
    private CartFeignClient cartFeignClient;
    @Resource
    private OrderInfoService orderInfoService;
    @Resource
    private ProductFeignClient productFeignClient;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;


    //api/order
    @GetMapping("auth/trade")
    public Result<Map<String, Object>> goTrade(HttpServletRequest request) {
        //获取用户id
        Long userId = Long.valueOf(AuthContextHolder.getUserId(request));
        //获取用户地址
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);
        // 渲染送货清单
        // 先得到用户想要购买的商品！
        List<CartInfo> cartInfoList = cartFeignClient.getCartCheckedList(userId);
        // 声明一个集合来存储订单明细
        List<OrderDetail> detailArrayList = new ArrayList<>();
        for (CartInfo cartInfo : cartInfoList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            // 添加到集合
            detailArrayList.add(orderDetail);
        }

        // 计算总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();
        //获取流水号
        String tradeNo = orderInfoService.getTradeNo(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("tradeNo", tradeNo);
        result.put("userAddressList", userAddressList);
        result.put("detailArrayList", detailArrayList);
        // 保存总金额
        result.put("totalNum", detailArrayList.size());
        result.put("totalAmount", orderInfo.getTotalAmount());
        return Result.ok(result);
    }

    @PostMapping("/auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,
                              HttpServletRequest request) {
        // 获取到用户Id
        Long userId = Long.valueOf(AuthContextHolder.getUserId(request));
        orderInfo.setUserId(userId);
        // 获取前台页面的流水号
        String tradeNo = request.getParameter("tradeNo");
        // 调用服务层的比较方法
        Boolean flag = orderInfoService.checkTradeCode(userId, tradeNo);
        if (!flag) {
            return Result.fail().message("不能重复提交订单！");
        }
        //  删除流水号
        orderInfoService.deleteTradeNo(userId);

        List<String> errorList = new ArrayList<>();
        ArrayList<CompletableFuture> futureList = new ArrayList<>();
        // 验证库存：
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            CompletableFuture<Void> checkStockCompletableFuture = CompletableFuture.runAsync(() -> {
                boolean result = orderInfoService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                if (!result) {
                    errorList.add(orderDetail.getSkuName() + "库存不足");
                }
            }, threadPoolExecutor);
            futureList.add(checkStockCompletableFuture);

            // 验证价格：
            CompletableFuture<Void> checkPriceCompletableFuture = CompletableFuture.runAsync(() -> {

                BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
                if (orderDetail.getOrderPrice().compareTo(skuPrice) != 0) {
                    // 重新查询价格！
                    //  设置最新的价格
                    List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
                    //  写入缓存：
                    cartCheckedList.forEach(cartInfo -> {
                        redisTemplate.opsForHash().put(RedisConst.USER_KEY_PREFIX + userId
                                + RedisConst.USER_CART_KEY_SUFFIX, cartInfo.getSkuId().toString(), cartInfo);
                    });
                    errorList.add(orderDetail.getSkuName() + "价格有变动！");
                }
            }, threadPoolExecutor);
            futureList.add(checkPriceCompletableFuture);
        }
        //合并线程
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();
        if (errorList.size() > 0) {
            return Result.fail().message(StringUtils.join(errorList, ","));
        }

        // 验证通过，保存订单！
        Long orderId = orderInfoService.saveOrderInfo(orderInfo);
        return Result.ok(orderId);


    }

    @GetMapping("/auth/{page}/{limit}")
    public Result myOrder(@PathVariable Long page,
                          @PathVariable Long limit,
                          HttpServletRequest request) {

        String userId = AuthContextHolder.getUserId(request);
        Page<OrderInfo> pageParam = new Page<>(page,limit);

        IPage<OrderInfo> page1 = orderInfoService.getMyOrder(pageParam,userId);
        return Result.ok(page1);
    }
}