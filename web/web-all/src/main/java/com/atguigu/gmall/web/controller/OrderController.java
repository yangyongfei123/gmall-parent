package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.Map;

@Controller
public class OrderController {
    @Resource
    private OrderFeignClient orderFeignClient;

    /**
     * 确认订单
     *
     * @param model
     */
    @GetMapping("trade.html")
    public String trade(Model model) {
        Result<Map<String, Object>> result = orderFeignClient.goTrade();
        model.addAttribute(result.getData());
        return "order/trade";
    }

    /**
     * 我的订单
     *
     * @return
     */
    @GetMapping("myOrder.html")
    public String myOrder() {
        return "order/myOrder";
    }
}
