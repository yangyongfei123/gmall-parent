package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartApiService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/cart/")
public class CartApiController {

    @Autowired
    private CartApiService cartApiService;


    /**
     * 添加购物车
     * /api/cart/addToCart/{skuId}/{skuNum}
     *
     * @param skuId
     * @param skuNum
     * @param request
     * @return
     */

    @ApiOperation("添加购物车")
    @GetMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable Long skuId,
                            @PathVariable Integer skuNum,
                            HttpServletRequest request) {

        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartApiService.addToCart(skuId, userId, skuNum);
        return Result.ok();
    }

    /**
     * 查询购物车
     * /api/cart/
     *
     * @param request
     * @return
     */
    @GetMapping("cartList")
    public Result cartList(HttpServletRequest request) {
        // 获取用户Id
        String userId = AuthContextHolder.getUserId(request);
        // 获取临时用户Id
        String userTempId = AuthContextHolder.getUserTempId(request);
        List<CartInfo> cartInfoList = cartApiService.getCartList(userId, userTempId);
        return Result.ok(cartInfoList);
    }

    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked,
                            HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        if (!StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserId(request);
        }
        //  调用服务层方法
        cartApiService.checkCart(userId, isChecked, skuId);

        return Result.ok();
    }

    /**
     * 删除
     * /api/cart/
     *
     * @param skuId
     * @param request
     * @return
     */
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable Long skuId,
                             HttpServletRequest request) {
        // 获取userId
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            // 获取临时用户Id
            userId = AuthContextHolder.getUserTempId(request);
        }

        cartApiService.deleteCart(userId, skuId);
        return Result.ok();
    }

    /**
     * 获取选中商品的信息
     * /api/cart/getCartCheckedList/{userId}
     *
     * @param userId
     * @return
     */
    @GetMapping("getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable Long userId) {
       List<CartInfo> cartInfoList=cartApiService.getCartCheckedList(userId);
       return cartInfoList;
    }
}
