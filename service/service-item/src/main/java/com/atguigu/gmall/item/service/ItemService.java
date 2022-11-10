package com.atguigu.gmall.item.service;

import java.util.Map;

public interface ItemService {
    /**
     * 商品详情
     * @param skuId
     * @return
     */
    Map<String, Object> getItem(Long skuId);
}
