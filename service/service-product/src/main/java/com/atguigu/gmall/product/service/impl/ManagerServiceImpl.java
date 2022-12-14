package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManagerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("all")

public class ManagerServiceImpl implements ManagerService {


    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuPosterMapper spuPosterMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;
    @Autowired
    private RabbitService rabbitService;

    /**
     * ????????????????????????
     *
     * @return
     */
    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    /**
     * ??????????????????id????????????????????????
     *
     * @param category1Id
     * @return
     */
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        //select *from base_category2 where category1_id=category1Id

        //????????????
        QueryWrapper<BaseCategory2> queryWrapper = new QueryWrapper<>();
        //????????????
        queryWrapper.eq("category1_id", category1Id);

        List<BaseCategory2> category2List = baseCategory2Mapper.selectList(queryWrapper);


        return category2List;
    }

    /**
     * ??????????????????id????????????????????????
     *
     * @param category2Id
     * @return
     */
    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        QueryWrapper<BaseCategory3> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category2_id", category2Id);

        List<BaseCategory3> baseCategory3List = baseCategory3Mapper.selectList(queryWrapper);


        return baseCategory3List;
    }

    /**
     * ????????????Id ????????????????????????
     *
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id) {


        return baseAttrInfoMapper.attrInfoList(category1Id, category2Id, category3Id);
    }


    /**
     * ??????-??????????????????
     *
     * @param baseAttrInfo ???????????????????????????????????????????????????
     *                     baseAttrInfo??????Id
     *                     <p>
     *                     ????????????
     *                     ???????????????
     * @Transactional: ???????????????
     * <p>
     * IOException
     * SQLException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        //??????????????????
        if (baseAttrInfo.getId() != null) {
            //??????
            baseAttrInfoMapper.updateById(baseAttrInfo);

            //????????????????????????????????????
            QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("attr_id", baseAttrInfo.getId());


            baseAttrValueMapper.delete(queryWrapper);


        } else {
            //??????
            baseAttrInfoMapper.insert(baseAttrInfo);

        }


        //?????????????????????
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        //??????
        if (!CollectionUtils.isEmpty(attrValueList)) {

            for (BaseAttrValue baseAttrValue : attrValueList) {

                //??????????????????????????????
                baseAttrValue.setAttrId(baseAttrInfo.getId());

                baseAttrValueMapper.insert(baseAttrValue);
            }


        }

    }

    /**
     * ???????????????????????????
     *
     * @param attrId
     * @return
     */
    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {

        //????????????
        QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("attr_id", attrId);

        return baseAttrValueMapper.selectList(queryWrapper);
    }

    /**
     * ????????????spu??????
     *
     * @param spuInfoPage
     * @param spuInfo
     * @return
     */
    @Override
    public IPage<SpuInfo> getSpuInfoByPage(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo) {

        //select *from spu_info where category3_id=61
        QueryWrapper<SpuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id", spuInfo.getCategory3Id());

        return spuInfoMapper.selectPage(spuInfoPage, queryWrapper);
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    /**
     * ??????spu
     * <p>
     * ???????????????
     * 1.spu_info
     * 2.spu_image
     * 3.spu_poster
     * 4.spu_sale_attr
     * 5.spu_sale_attr_value
     *
     * @param spuInfo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {

        //??????spuId
        spuInfoMapper.insert(spuInfo);
        //????????????
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        //??????
        if (!CollectionUtils.isEmpty(spuImageList)) {

            for (SpuImage spuImage : spuImageList) {

                //??????spu_id
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insert(spuImage);
            }


        }

        //spu_poster??????
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        if (!CollectionUtils.isEmpty(spuPosterList)) {

            for (SpuPoster spuPoster : spuPosterList) {

                //??????spuId
                spuPoster.setSpuId(spuInfo.getId());
                spuPosterMapper.insert(spuPoster);
            }

        }

        //spu_sale_attr ????????????
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (!CollectionUtils.isEmpty(spuSaleAttrList)) {

            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {

                //??????spuId
                spuSaleAttr.setSpuId(spuInfo.getId());

                spuSaleAttrMapper.insert(spuSaleAttr);

                //??????spu_sale_attr_value
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                //??????
                if (!CollectionUtils.isEmpty(spuSaleAttrValueList)) {

                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {

                        //spu_id
                        spuSaleAttrValue.setSpuId(spuInfo.getId());

                        //??????????????????
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());

                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }

                }


            }
        }

    }

    /**
     * ??????spuId ??????spuImage ??????
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuImage> spuImageList(Long spuId) {
        //select*from spu_image where spu_id =?
        //?????? ??????
        QueryWrapper<SpuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id", spuId);


        return spuImageMapper.selectList(queryWrapper);
    }

    /**
     * ??????spuId ??????????????????
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {

        return spuSaleAttrMapper.spuSaleAttrList(spuId);
    }


    @Autowired
    private RedissonClient redissonClient;

    /**
     * ??????skuInfo
     *
     * @param skuInfo ???????????????
     *                1.sku_info
     *                2.sku_image
     *                3.sku_sale_attr_value
     *                4.sku_attr_value
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {

        //??????sku
        skuInfoMapper.insert(skuInfo);
        //????????????
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        //??????
        if (!CollectionUtils.isEmpty(skuImageList)) {
            for (SkuImage skuImage : skuImageList) {

                //??????skuid
                skuImage.setSkuId(skuInfo.getId());

                skuImageMapper.insert(skuImage);
            }


        }

        //??????????????????
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        //??????
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {

                //??????spuid
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                //??????skuid
                skuSaleAttrValue.setSkuId(skuInfo.getId());


                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }

        }

        //??????????????????
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        //??????
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {

            skuAttrValueList.stream().forEach(skuAttrValue -> {

                skuAttrValue.setSkuId(skuInfo.getId());

                skuAttrValueMapper.insert(skuAttrValue);
            });
        }

        //????????????????????????

//        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
//        bloomFilter.add(skuInfo.getId());

    }

    /**
     * skuinfo????????????
     *
     * @param skuInfoPage
     * @return
     */
    @Override
    public IPage<SkuInfo> getSkuInfoByPage(Page<SkuInfo> skuInfoPage) {
        //??????
        QueryWrapper<SkuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");

        return skuInfoMapper.selectPage(skuInfoPage, queryWrapper);
    }

    /**
     * ??????
     *
     * @param skuId
     */
    @Override
    @Transactional
    public void cancelSale(Long skuId) {

        //????????????
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);

        skuInfoMapper.updateById(skuInfo);

        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_GOODS, MqConst.ROUTING_GOODS_LOWER, skuId);

    }

    /**
     * ??????
     *
     * @param skuId
     */
    @Override
    @Transactional
    public void onSale(Long skuId) {
        //????????????
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);

        skuInfoMapper.updateById(skuInfo);

        //???????????????????????????
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_GOODS, MqConst.ROUTING_GOODS_UPPER, skuId);

    }


    public static void main(String[] args) {

        String msg = "aaa";
        Object obj = msg;

//        Object obj=new Object();

        String aaa = (String) obj;

        System.out.println(aaa);
    }

    /**
     * ??????skuId??????SkuInfo
     *
     * @param skuId
     * @return
     */
    @Override
    @GmallCache(prefix = "sku:", suffix = ":info")
    public SkuInfo getSkuInfo(Long skuId) {


        return getSkuInfoDB(skuId);
//        return getSkuInfoRedis(skuId);

//        return getSkuinfoReddisson(skuId);
    }

//    @Autowired
//    private RedissonClient redissonClient;

    /**
     * redisson????????????????????????????????????
     * <p>
     * 1.??????
     * Rlock lock=redisson.getLock(skuLock);
     * boolean result=lock.try(??????????????????????????????????????????????????????);
     * <p>
     * 2.?????????
     * lock.unlock();
     * <p>
     * 3.????????????
     *
     * @param skuId
     * @return
     */
    private SkuInfo getSkuinfoReddisson(Long skuId) {

        try {
            //?????????????????????key
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            //?????????redis????????????
            SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            //??????
            if (skuInfo == null) {
                //?????????key
                String skuLock = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                //?????????
                RLock lock = redissonClient.getLock(skuLock);
                //?????????
                boolean result = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //??????
                if (result) {
                    try {
                        //???????????????
                        skuInfo = this.getSkuInfoDB(skuId);
                        //??????
                        if (skuInfo == null) {
                            skuInfo = new SkuInfo();
                            redisTemplate.opsForValue().set(skuKey, skuInfo);

                            return skuInfo;

                        } else {
                            redisTemplate.opsForValue().set(skuKey, skuInfo);

                            return skuInfo;
                        }
                    } finally {

                        lock.unlock();
                    }


                } else {
                    //??????
                    Thread.sleep(100);
                    return this.getSkuinfoReddisson(skuId);

                }


            } else {

                //???????????????
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //???????????????
        return this.getSkuInfoDB(skuId);
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * ??????redis??????????????????+??????
     * ???????????????
     * try{
     * 1.??????????????????key
     * *  2.?????????redis???????????????
     * *      ??????????????????
     * *      ?????????
     * *       3.?????????key,???????????????
     * *         ?????????
     * *          ???????????????
     * *          ??????
     * *           4.?????????????????????
     * *             ????????????????????????????????????
     * *             ???????????????????????????????????????
     * *            5.?????????
     * *
     * <p>
     * }catch{
     * <p>
     * <p>
     * <p>
     * }
     * <p>
     * ???????????????????????????getSkuInfoDB
     *
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoRedis(Long skuId) {
        //????????????
        SkuInfo skuInfo = null;

        try {
            //????????????key
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            //???redis????????????????????????
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            //??????
            if (skuInfo == null) {

                //???????????????????????????
                //????????????key
                String skuLock = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                //??????
                String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                //?????????
                Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(skuLock, uuid, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //???????????????????????????
                if (aBoolean) {
                    try {
                        //?????????????????????????????????
                        skuInfo = getSkuInfoDB(skuId);
                        //??????mysql??????????????????
                        if (skuInfo == null) {
                            skuInfo = new SkuInfo();

                            redisTemplate.opsForValue().set(skuKey, skuInfo);

                            return skuInfo;

                        } else {
                            //??????????????????
                            redisTemplate.opsForValue().set(skuKey, skuInfo);
                            return skuInfo;
                        }
                    } finally {
                        //??????lua??????
                        String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                                "then\n" +
                                "    return redis.call(\"del\",KEYS[1])\n" +
                                "else\n" +
                                "    return 0\n" +
                                "end";

                        //??????lua??????
                        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                        redisScript.setScriptText(script);
                        redisScript.setResultType(Long.class);
                        //??????lua??????
                        redisTemplate.execute(redisScript, Arrays.asList(skuLock), uuid);

                    }


                } else {
                    //??????????????????
                    Thread.sleep(100);
                    return this.getSkuInfoRedis(skuId);

                }


            } else {

                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //???????????????
        return this.getSkuInfoDB(skuId);
    }


    /**
     * ???????????????
     *
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoDB(Long skuId) {
        //??????skuInfo
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        //??????skuid???????????????
        QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<SkuImage>();
        queryWrapper.eq("sku_id", skuId);
        List<SkuImage> skuImages = skuImageMapper.selectList(queryWrapper);
        //?????????skuInfo
        if (skuInfo != null) {
            skuInfo.setSkuImageList(skuImages);
        }
        return skuInfo;
    }

    /**
     * ??????skuId ???????????????????????????
     *
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {

        try {
            RLock lock = redissonClient.getLock(RedisConst.SKUKEY_PREFIX + skuId);

            //??????
            boolean result = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
            if (result) {

                try {
                    SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
                    //??????
                    if (skuInfo != null) {

                        return skuInfo.getPrice();
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                Thread.sleep(100);
                this.getSkuPrice(skuId);


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new BigDecimal("0");
    }

    /**
     * ??????spuId ??????????????????
     *
     * @param spuId
     * @return
     */
    @Override
    @GmallCache(prefix = "findSpuPosterBySpuId:", suffix = ":info")
    public List<SpuPoster> findSpuPosterBySpuId(Long spuId) {

        //select *from spu_poster where spu_id =spuId;
        QueryWrapper<SpuPoster> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id", spuId);
        return spuPosterMapper.selectList(queryWrapper);
    }

    /**
     * ??????????????????id??????????????????
     *
     * @param category3Id
     * @return
     */
    @Override
    @GmallCache(prefix = "getCategoryView:", suffix = ":info")
    public BaseCategoryView getCategoryView(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    /**
     * ??????skuId ????????????????????????
     *
     * @param skuId
     * @return
     */
    @Override
    @GmallCache(prefix = "getAttrList:", suffix = ":info")
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return baseAttrInfoMapper.getAttrList(skuId);
    }

    /**
     * ??????spuId,skuId ????????????????????????
     *
     * @param skuId
     * @param spuId
     * @return
     */
    @Override
    @GmallCache(prefix = "getSpuSaleAttrListCheckBySku:", suffix = ":info")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {

        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    /**
     * ??????spuId ????????????????????????Id ???skuId ??????????????????
     *
     * @param spuId
     * @return
     */
    @Override
    @GmallCache(prefix = "getSkuValueIdsMap:", suffix = ":info")
    public Map getSkuValueIdsMap(Long spuId) {
        Map resultMap = new HashMap();
        List<Map> mapList = skuSaleAttrValueMapper.selectSkuValueIdsMap(spuId);
        //??????
        if (!CollectionUtils.isEmpty(mapList)) {

            for (Map map : mapList) {
                resultMap.put(map.get("value_ids"), map.get("sku_id"));

            }

        }

        return resultMap;
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    @Override
    @GmallCache(prefix = "baseCategoryLis:")
    public List<JSONObject> getBaseCategoryList() {

        //????????????????????????
        List<JSONObject> list = new ArrayList<>();
        //????????????????????????
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
        //??????
        Map<Long, List<BaseCategoryView>> categoryMap1 = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        //????????????
        int index = 1;
        //??????map
        for (Map.Entry<Long, List<BaseCategoryView>> entry : categoryMap1.entrySet()) {

            //????????????????????????
            JSONObject jsonObject1 = new JSONObject();

            //????????????id
            Long category1Id = entry.getKey();
            //??????????????????
            List<BaseCategoryView> category2List = entry.getValue();
            String category1Name = category2List.get(0).getCategory1Name();
            jsonObject1.put("categoryId", category1Id);
            jsonObject1.put("categoryName", category1Name);
            jsonObject1.put("index", index);
            index++;

            //??????????????????
            Map<Long, List<BaseCategoryView>> categoryMap2 = category2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));

            //?????????????????????????????????
            List<JSONObject> list2 = new ArrayList<>();

            //???????????????
            for (Map.Entry<Long, List<BaseCategoryView>> entry2 : categoryMap2.entrySet()) {
                JSONObject jsonObject2 = new JSONObject();
                //?????????????????????id
                Long category2Id = entry2.getKey();
                //?????????????????????name
                List<BaseCategoryView> category3List = entry2.getValue();
                String category2Name = category3List.get(0).getCategory2Name();
                jsonObject2.put("categoryId", category2Id);
                jsonObject2.put("categoryName", category2Name);

                //??????????????????
                List<JSONObject> list3 = new ArrayList<>();
                //??????????????????
                for (BaseCategoryView baseCategoryView : category3List) {
                    JSONObject jsonObject3 = new JSONObject();
                    jsonObject3.put("categoryId", baseCategoryView.getCategory3Id());
                    jsonObject3.put("categoryName", baseCategoryView.getCategory3Name());
                    list3.add(jsonObject3);
                }
                jsonObject2.put("categoryChild", list3);

                //??????????????????
                list2.add(jsonObject2);

            }

            jsonObject1.put("categoryChild", list2);

            list.add(jsonObject1);


        }

        return list;
    }


}
