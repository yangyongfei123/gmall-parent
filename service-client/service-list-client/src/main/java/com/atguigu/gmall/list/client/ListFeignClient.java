package com.atguigu.gmall.list.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.impl.ListDegradeFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-list",fallback = ListDegradeFeignClient.class)
public interface ListFeignClient  {


    /**
     *  /api/list
     * 商品搜索
     * @return
     */
    @PostMapping("/api/list")
    public Result search(@RequestBody SearchParam searchParam);
    /**
     * api/list/inner/incrHotScore/{skuId}
     * 商品热度排名统计
     * @param skuId
     * @return
     */
    @GetMapping("/api/list/inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable Long skuId);
}
