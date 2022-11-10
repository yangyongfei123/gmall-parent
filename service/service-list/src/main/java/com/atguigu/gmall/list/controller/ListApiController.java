package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/list")
public class ListApiController {


    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private SearchService searchService;



    /**
     *  /api/list
     * 商品搜索
     * @return
     */
    @PostMapping
    public Result search(@RequestBody SearchParam searchParam){
       SearchResponseVo searchResponseVo= searchService.search(searchParam);
        return Result.ok(searchResponseVo);
    }



    /**
     * api/list/inner/incrHotScore/{skuId}
     * 商品热度排名统计
     * @param skuId
     * @return
     */
    @GetMapping("inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable Long skuId){
        searchService.incrHotScore(skuId);

        return Result.ok();


    }

    /**
     * //api/list/inner/lowerGoods/{skuId}
     * 商品下架
     * @param skuId
     * @return
     */
    @GetMapping("inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable Long skuId){

        searchService.lowerGoods(skuId);
        return Result.ok();
    }


    /**
     * ///api/list/inner/upperGoods/{skuId}
     * 商品上架
     * @return
     */
    @GetMapping("/inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable Long skuId){
        searchService.upperGoods(skuId);

        return Result.ok();
    }


    /**
     * /api/list/createIndex
     * 创建索引库
     * @return
     */
    @GetMapping("/createIndex")
    public Result createIndex(){
        //创建索引库
        restTemplate.createIndex(Goods.class);
        //创建mapping结构
        restTemplate.putMapping(Goods.class);


        return Result.ok();
    }
}
