package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("all")
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ListFeignClient listFeignClient;

    @Autowired
    private ThreadPoolExecutor executor;
    /**
     * 商品详情
     * @param skuId
     * @return
     */
    @Override
    public Map<String, Object> getItem(Long skuId) {



        //创建封装结果集对象
        Map<String, Object> resultMap=new HashMap<>();
        //获取过滤器
//        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
//        //判断
//        if(!bloomFilter.contains(skuId)){
//            return resultMap;
//        }
        //设置skuInfo和图片集合
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            resultMap.put("skuInfo", skuInfo);
            return skuInfo;
        }, executor);

        CompletableFuture<Void> categoryViewfuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {

            if (skuInfo != null) {

                //查询三级分类
                BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
                resultMap.put("categoryView",categoryView);
            }


        }, executor);


        //查询海报

        CompletableFuture<Void> posterBySpuIdfuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {

            List<SpuPoster> spuPosterBySpuId = productFeignClient.findSpuPosterBySpuId(skuInfo.getSpuId());
            resultMap.put("spuPosterList", spuPosterBySpuId);

        }, executor);


        CompletableFuture<Void> spuSaleAttrListCheckBySkufuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {

            //查询销售属性显示
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            resultMap.put("spuSaleAttrList", spuSaleAttrListCheckBySku);

        }, executor);

        CompletableFuture<Void> skuValueIdsMapfuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {

            //查询销售属性和skuId的关系映射
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            resultMap.put("valuesSkuJson", JSON.toJSONString(skuValueIdsMap));
        }, executor);


        CompletableFuture<Void> skuPricefuture = CompletableFuture.runAsync(() -> {

            //获取价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            resultMap.put("price", skuPrice);

        }, executor);


        CompletableFuture<Void> attrListfuture = CompletableFuture.runAsync(() -> {

            //查询平台属性
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);//判断
            if(!CollectionUtils.isEmpty(attrList)){

                List<Map<String, String>> mapList = attrList.stream().map(baseAttrInfo -> {
                    //创建封装对象map
                    Map<String, String> attrMap = new HashMap<>();
                    //设置属性
                    attrMap.put("attrName", baseAttrInfo.getAttrName());
                    attrMap.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());

                    return attrMap;


                }).collect(Collectors.toList());

                resultMap.put("skuAttrList",mapList);
            }



        }, executor);


        //热度统计
        CompletableFuture<Void> incrHotScorefuture = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);

        }, executor);


        //组合编排
        CompletableFuture.allOf(
                skuInfoCompletableFuture,
                categoryViewfuture,
                skuPricefuture,
                posterBySpuIdfuture,
                attrListfuture,
                skuValueIdsMapfuture,
                spuSaleAttrListCheckBySkufuture,
                incrHotScorefuture
        ).join();





        return resultMap;
    }
}
