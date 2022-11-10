package com.atguigu.gmall.order.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.client.impl.OrderDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@FeignClient(name = "service-order", fallback = OrderDegradeFeignClient.class)
public interface OrderFeignClient {
    //api/order/
    @GetMapping("/api/order/auth/trade")
    public Result<Map<String, Object>> goTrade();
}
