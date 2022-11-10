package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ManagerService  {

    /**
     * 查询一级分类列表
     * @return
     */
    List<BaseCategory1> getCategory1();

    /**
     * 根据一级分类id查询二级分类列表
     * @param category1Id
     * @return
     */
    List<BaseCategory2> getCategory2(Long category1Id);

    /**
     * 根据二级分类id查询三级分类列表
     * @param category2Id
     * @return
     */
    List<BaseCategory3> getCategory3(Long category2Id);

    /**
     * 根据分类Id 获取平台属性集合
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id);

    /**
     * 保存-修改平台属性
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 获取平台属性值集合
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(Long attrId);

    /**
     * 分页查询spu列表
     * @param spuInfoPage
     * @param spuInfo
     * @return
     */
    IPage<SpuInfo> getSpuInfoByPage(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo);

    /**
     * 获取销售属性
     * @return
     */
    List<BaseSaleAttr> baseSaleAttrList();


    /**
     * 保存spu
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据spuId 获取spuImage 集合
     * @param spuId
     * @return
     */
    List<SpuImage> spuImageList(Long spuId);

    /**
     * 根据spuId 查询销售属性
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> spuSaleAttrList(Long spuId);

    /**
     * 保存skuInfo
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * skuinfo分页查询
     * @param skuInfoPage
     * @return
     */
    IPage<SkuInfo> getSkuInfoByPage(Page<SkuInfo> skuInfoPage);

    /**
     * 下架
     * @param skuId
     */
    void cancelSale(Long skuId);

    /**
     * 上架
     * @param skuId
     */
    void onSale(Long skuId);

    /**
     * 根据skuId获取SkuInfo
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(Long skuId);

    /**
     * 根据skuId 获取最新的商品价格
     * @param skuId
     * @return
     */
    BigDecimal getSkuPrice(Long skuId);

    /**
     * 根据spuId 获取海报数据
     * @param spuId
     * @return
     */
    List<SpuPoster> findSpuPosterBySpuId(Long spuId);

    /**
     * 根据三级分类id获取分类信息
     * @param category3Id
     * @return
     */
    BaseCategoryView getCategoryView(Long category3Id);

    /**
     * 根据skuId 获取平台属性数据
     * @param skuId
     * @return
     */
    List<BaseAttrInfo> getAttrList(Long skuId);


    /**
     * 根据spuId,skuId 获取销售属性数据
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    /**
     * 根据spuId 获取到销售属性值Id 与skuId 组成的数据集
     * @param spuId
     * @return
     */
    Map getSkuValueIdsMap(Long spuId);

    /**
     *  获取首页分类数据
     * @return
     */
    List<JSONObject> getBaseCategoryList();


}
