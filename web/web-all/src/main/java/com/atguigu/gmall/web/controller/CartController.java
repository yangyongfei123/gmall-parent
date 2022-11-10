package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 购物车页面
 */
@Controller
public class CartController {
    @Resource
    private ProductFeignClient productFeignClient;
    @Resource
    private CartFeignClient cartFeignClient;

    /**
     * 查看购物车
     *
     * @return
     */
    @RequestMapping("cart.html")
    public String index() {
        return "cart/index";
    }

    @RequestMapping("addCart.html")
    public String addCart(@RequestParam(name = "skuId") Long skuId,
                          @RequestParam(name = "skuNum") Integer skuNum,
                          HttpServletRequest request) {

        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("skuNum", skuNum);

        return "cart/addCart";
    }
}
