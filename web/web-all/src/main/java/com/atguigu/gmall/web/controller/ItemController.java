package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
@SuppressWarnings("all")
public class ItemController {

    @Autowired
    private ItemFeignClient itemFeignClient;

    /**
     * 商品详情页
     * @return
     */
    @GetMapping("/{skuId}.html")
    public  String item(@PathVariable Long skuId, Model model){
        //获取详情页需要显示的数据
        Result<Map<String,Object>> result = itemFeignClient.getItem(skuId);
        //将map中的key-value进行响应
        model.addAllAttributes(result.getData());
        return "item/item";
    }
}
