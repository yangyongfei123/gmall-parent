package com.atguigu.gmall.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.BaseTradeMarkService;
import com.atguigu.gmall.product.service.ManagerService;
import org.simpleframework.xml.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductApiController {

    @Autowired
    private ManagerService managerService;

    @Autowired
    private BaseTradeMarkService baseTradeMarkService;


    /**
     * 根据品牌Id 获取品牌数据
     *   api/product/inner/getTrademark/{tmId}
     *
     * @param tmId
     * @return
     */
    @GetMapping("/inner/getTrademark/{tmId}")
    public BaseTrademark getTrademark(@PathVariable Long tmId){

        return baseTradeMarkService.getById(tmId);
    }

    /**
     * /api/product/getBaseCategoryList
     * 获取首页分类数据
     * @return
     */
    @GetMapping("/getBaseCategoryList")
    public Result getBaseCategoryList(){



        List<JSONObject> listMap=managerService.getBaseCategoryList();

        return Result.ok(listMap);
    }

    /**
     * /api/product/inner/getSkuValueIdsMap/{spuId}
     * 根据spuId 获取到销售属性值Id 与skuId 组成的数据集
     * @param spuId
     * @return
     */
    @GetMapping("/inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId){


        return managerService.getSkuValueIdsMap(spuId);
    }

    /**
     * //api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}
     * 根据spuId,skuId 获取销售属性数据
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,
                                                          @PathVariable  Long spuId){


        return managerService.getSpuSaleAttrListCheckBySku(skuId,spuId);
    }



    /**
     * ///api/product/inner/getAttrList/{skuId}
     * 根据skuId 获取平台属性数据
     * @param skuId
     * @return
     */
    @GetMapping("/inner/getAttrList/{skuId}")
    public  List<BaseAttrInfo> getAttrList(@PathVariable Long skuId){


        return managerService.getAttrList(skuId);
    }

    /**
     * /api/product/inner/getCategoryView/{category3Id}
     * 根据三级分类id获取分类信息
     * @param category3Id
     * @return
     */
    @GetMapping("/inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id ){


        return managerService.getCategoryView(category3Id);
    }

    /**
     * //api/product/inner/findSpuPosterBySpuId/{spuId}
     * 根据spuId 获取海报数据
     * @param spuId
     * @return
     */
    @GetMapping("/inner/findSpuPosterBySpuId/{spuId}")
    public List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId){


        return managerService.findSpuPosterBySpuId(spuId);
    }


    /**
     * /api/product/inner/getSkuPrice/{skuId}
     * 根据skuId 获取最新的商品价格
     * @param skuId
     * @return
     */
    @GetMapping("/inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId){


        return managerService.getSkuPrice(skuId);
    }


    /**
     * /api/product/inner/getSkuInfo/{skuId}
     * 根据skuId获取SkuInfo
     * @param skuId
     * @return
     */
    @GetMapping("/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId){

        return managerService.getSkuInfo(skuId);
    }



}
