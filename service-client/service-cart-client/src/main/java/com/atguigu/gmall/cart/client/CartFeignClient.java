package com.atguigu.gmall.cart.client;

import com.atguigu.gmall.cart.client.impl.CartDgradeFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@FeignClient(value = "service-cart", fallback = CartDgradeFeignClient.class)
public interface CartFeignClient {
    @ApiOperation("添加购物车")
    @GetMapping("/api/cart/addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable Long skuId,
                            @PathVariable Integer skuNum,
                            HttpServletRequest request);
    /**
     * 查询购物车
     * /api/cart/
     *
     * @param request
     * @return
     */
    @GetMapping("/api/cart/cartList")
    public Result cartList(HttpServletRequest request);

    @GetMapping("/api/cart/checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked,
                            HttpServletRequest request);
    /**
     * 删除
     * /api/cart/
     *
     * @param skuId
     * @param request
     * @return
     */
    @DeleteMapping("/api/cart/deleteCart/{skuId}")
    public Result deleteCart(@PathVariable Long skuId,
                             HttpServletRequest request);

    /**
     * 获取选中商品的信息
     * /api/cart/getCartCheckedList/{userId}
     *
     * @param userId
     * @return
     */
    @GetMapping("/api/cart/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable Long userId);
}
