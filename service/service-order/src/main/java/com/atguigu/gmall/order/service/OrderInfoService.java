package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;


public interface OrderInfoService extends IService<OrderInfo> {
    Long saveOrderInfo(OrderInfo orderInfo);

    String getTradeNo(Long userId);

    Boolean checkTradeCode(Long userId, String tradeNo);

    void deleteTradeNo(Long userId);

    boolean checkStock(Long skuId, Integer skuNum);

    IPage<OrderInfo> getMyOrder(Page<OrderInfo> pageParam, String userId);
}
