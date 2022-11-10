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
     * 查询一级分类列表
     *
     * @return
     */
    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    /**
     * 根据一级分类id查询二级分类列表
     *
     * @param category1Id
     * @return
     */
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        //select *from base_category2 where category1_id=category1Id

        //封装条件
        QueryWrapper<BaseCategory2> queryWrapper = new QueryWrapper<>();
        //设置条件
        queryWrapper.eq("category1_id", category1Id);

        List<BaseCategory2> category2List = baseCategory2Mapper.selectList(queryWrapper);


        return category2List;
    }

    /**
     * 根据二级分类id查询三级分类列表
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
     * 根据分类Id 获取平台属性集合
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
     * 保存-修改平台属性
     *
     * @param baseAttrInfo 确定是保存操作还是修改操作的关键：
     *                     baseAttrInfo中的Id
     *                     <p>
     *                     有：修改
     *                     没有：添加
     * @Transactional: 运行时异常
     * <p>
     * IOException
     * SQLException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        //保存平台属性
        if (baseAttrInfo.getId() != null) {
            //修改
            baseAttrInfoMapper.updateById(baseAttrInfo);

            //删除原来关联的平台属性值
            QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("attr_id", baseAttrInfo.getId());


            baseAttrValueMapper.delete(queryWrapper);


        } else {
            //添加
            baseAttrInfoMapper.insert(baseAttrInfo);

        }


        //添加平台属性值
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        //保存
        if (!CollectionUtils.isEmpty(attrValueList)) {

            for (BaseAttrValue baseAttrValue : attrValueList) {

                //设置关联的平台属性值
                baseAttrValue.setAttrId(baseAttrInfo.getId());

                baseAttrValueMapper.insert(baseAttrValue);
            }


        }

    }

    /**
     * 获取平台属性值集合
     *
     * @param attrId
     * @return
     */
    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {

        //封装条件
        QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("attr_id", attrId);

        return baseAttrValueMapper.selectList(queryWrapper);
    }

    /**
     * 分页查询spu列表
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
     * 获取销售属性
     *
     * @return
     */
    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    /**
     * 保存spu
     * <p>
     * 操作的表：
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

        //保存spuId
        spuInfoMapper.insert(spuInfo);
        //保存图片
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        //判断
        if (!CollectionUtils.isEmpty(spuImageList)) {

            for (SpuImage spuImage : spuImageList) {

                //设置spu_id
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insert(spuImage);
            }


        }

        //spu_poster海报
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        if (!CollectionUtils.isEmpty(spuPosterList)) {

            for (SpuPoster spuPoster : spuPosterList) {

                //设置spuId
                spuPoster.setSpuId(spuInfo.getId());
                spuPosterMapper.insert(spuPoster);
            }

        }

        //spu_sale_attr 销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (!CollectionUtils.isEmpty(spuSaleAttrList)) {

            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {

                //设置spuId
                spuSaleAttr.setSpuId(spuInfo.getId());

                spuSaleAttrMapper.insert(spuSaleAttr);

                //保存spu_sale_attr_value
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                //判断
                if (!CollectionUtils.isEmpty(spuSaleAttrValueList)) {

                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {

                        //spu_id
                        spuSaleAttrValue.setSpuId(spuInfo.getId());

                        //销售属性名称
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());

                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }

                }


            }
        }

    }

    /**
     * 根据spuId 获取spuImage 集合
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuImage> spuImageList(Long spuId) {
        //select*from spu_image where spu_id =?
        //封装 条件
        QueryWrapper<SpuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id", spuId);


        return spuImageMapper.selectList(queryWrapper);
    }

    /**
     * 根据spuId 查询销售属性
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
     * 保存skuInfo
     *
     * @param skuInfo 涉及的表：
     *                1.sku_info
     *                2.sku_image
     *                3.sku_sale_attr_value
     *                4.sku_attr_value
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {

        //保存sku
        skuInfoMapper.insert(skuInfo);
        //获取图片
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        //判断
        if (!CollectionUtils.isEmpty(skuImageList)) {
            for (SkuImage skuImage : skuImageList) {

                //设置skuid
                skuImage.setSkuId(skuInfo.getId());

                skuImageMapper.insert(skuImage);
            }


        }

        //获取销售属性
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        //判断
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {

                //设置spuid
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                //设置skuid
                skuSaleAttrValue.setSkuId(skuInfo.getId());


                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }

        }

        //获取平台户型
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        //判断
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {

            skuAttrValueList.stream().forEach(skuAttrValue -> {

                skuAttrValue.setSkuId(skuInfo.getId());

                skuAttrValueMapper.insert(skuAttrValue);
            });
        }

        //添加到布隆过滤器

//        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
//        bloomFilter.add(skuInfo.getId());

    }

    /**
     * skuinfo分页查询
     *
     * @param skuInfoPage
     * @return
     */
    @Override
    public IPage<SkuInfo> getSkuInfoByPage(Page<SkuInfo> skuInfoPage) {
        //排序
        QueryWrapper<SkuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");

        return skuInfoMapper.selectPage(skuInfoPage, queryWrapper);
    }

    /**
     * 下架
     *
     * @param skuId
     */
    @Override
    @Transactional
    public void cancelSale(Long skuId) {

        //创建对象
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);

        skuInfoMapper.updateById(skuInfo);

        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_GOODS, MqConst.ROUTING_GOODS_LOWER, skuId);

    }

    /**
     * 上架
     *
     * @param skuId
     */
    @Override
    @Transactional
    public void onSale(Long skuId) {
        //创建对象
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);

        skuInfoMapper.updateById(skuInfo);

        //发送商品上架的消息
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
     * 根据skuId获取SkuInfo
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
     * redisson实现分布式锁缓存优化实现
     * <p>
     * 1.加锁
     * Rlock lock=redisson.getLock(skuLock);
     * boolean result=lock.try(等待的最大时间，有效时间，时间的单位);
     * <p>
     * 2.释放锁
     * lock.unlock();
     * <p>
     * 3.自旋实现
     *
     * @param skuId
     * @return
     */
    private SkuInfo getSkuinfoReddisson(Long skuId) {

        try {
            //定义存储数据的key
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            //尝试从redis获取数据
            SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            //判断
            if (skuInfo == null) {
                //定义锁key
                String skuLock = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                //获取锁
                RLock lock = redissonClient.getLock(skuLock);
                //获取锁
                boolean result = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //判断
                if (result) {
                    try {
                        //获取到了锁
                        skuInfo = this.getSkuInfoDB(skuId);
                        //判断
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
                    //自旋
                    Thread.sleep(100);
                    return this.getSkuinfoReddisson(skuId);

                }


            } else {

                //缓存有数据
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //查询数据库
        return this.getSkuInfoDB(skuId);
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 使用redis实现分布式锁+缓存
     * 实现思路：
     * try{
     * 1.定义存储数据key
     * *  2.尝试从redis中获取数据
     * *      有：直接返回
     * *      没有：
     * *       3.定义锁key,尝试获取锁
     * *         没有：
     * *          睡眠，自旋
     * *          有：
     * *           4.调用数据库查询
     * *             有：存储到缓存，返回数据
     * *             没有：存储空对象，返回数据
     * *            5.释放锁
     * *
     * <p>
     * }catch{
     * <p>
     * <p>
     * <p>
     * }
     * <p>
     * 兜底方式：查询数据getSkuInfoDB
     *
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoRedis(Long skuId) {
        //定义变量
        SkuInfo skuInfo = null;

        try {
            //定义存储key
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            //从redis缓存尝试获取数据
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            //判断
            if (skuInfo == null) {

                //表示缓存中没有数据
                //定义锁的key
                String skuLock = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                //表示
                String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                //获取锁
                Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(skuLock, uuid, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //判断是否获取到了锁
                if (aBoolean) {
                    try {
                        //获取到了锁，查询数据库
                        skuInfo = getSkuInfoDB(skuId);
                        //判断mysql是否存在数据
                        if (skuInfo == null) {
                            skuInfo = new SkuInfo();

                            redisTemplate.opsForValue().set(skuKey, skuInfo);

                            return skuInfo;

                        } else {
                            //数据库有数据
                            redisTemplate.opsForValue().set(skuKey, skuInfo);
                            return skuInfo;
                        }
                    } finally {
                        //定义lua脚本
                        String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                                "then\n" +
                                "    return redis.call(\"del\",KEYS[1])\n" +
                                "else\n" +
                                "    return 0\n" +
                                "end";

                        //设置lua脚本
                        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                        redisScript.setScriptText(script);
                        redisScript.setResultType(Long.class);
                        //执行lua脚本
                        redisTemplate.execute(redisScript, Arrays.asList(skuLock), uuid);

                    }


                } else {
                    //没有获取到锁
                    Thread.sleep(100);
                    return this.getSkuInfoRedis(skuId);

                }


            } else {

                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //兜底的方法
        return this.getSkuInfoDB(skuId);
    }


    /**
     * 查询数据库
     *
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoDB(Long skuId) {
        //查询skuInfo
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        //查询skuid关联的图片
        QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<SkuImage>();
        queryWrapper.eq("sku_id", skuId);
        List<SkuImage> skuImages = skuImageMapper.selectList(queryWrapper);
        //设置到skuInfo
        if (skuInfo != null) {
            skuInfo.setSkuImageList(skuImages);
        }
        return skuInfo;
    }

    /**
     * 根据skuId 获取最新的商品价格
     *
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {

        try {
            RLock lock = redissonClient.getLock(RedisConst.SKUKEY_PREFIX + skuId);

            //加锁
            boolean result = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
            if (result) {

                try {
                    SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
                    //判断
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
     * 根据spuId 获取海报数据
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
     * 根据三级分类id获取分类信息
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
     * 根据skuId 获取平台属性数据
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
     * 根据spuId,skuId 获取销售属性数据
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
     * 根据spuId 获取到销售属性值Id 与skuId 组成的数据集
     *
     * @param spuId
     * @return
     */
    @Override
    @GmallCache(prefix = "getSkuValueIdsMap:", suffix = ":info")
    public Map getSkuValueIdsMap(Long spuId) {
        Map resultMap = new HashMap();
        List<Map> mapList = skuSaleAttrValueMapper.selectSkuValueIdsMap(spuId);
        //判断
        if (!CollectionUtils.isEmpty(mapList)) {

            for (Map map : mapList) {
                resultMap.put(map.get("value_ids"), map.get("sku_id"));

            }

        }

        return resultMap;
    }

    /**
     * 获取首页分类数据
     *
     * @return
     */
    @Override
    @GmallCache(prefix = "baseCategoryLis:")
    public List<JSONObject> getBaseCategoryList() {

        //创建封装集合对象
        List<JSONObject> list = new ArrayList<>();
        //获取所有分类数据
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
        //分组
        Map<Long, List<BaseCategoryView>> categoryMap1 = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        //定义序号
        int index = 1;
        //遍历map
        for (Map.Entry<Long, List<BaseCategoryView>> entry : categoryMap1.entrySet()) {

            //创建一级分类对象
            JSONObject jsonObject1 = new JSONObject();

            //一级分类id
            Long category1Id = entry.getKey();
            //一级分类名称
            List<BaseCategoryView> category2List = entry.getValue();
            String category1Name = category2List.get(0).getCategory1Name();
            jsonObject1.put("categoryId", category1Id);
            jsonObject1.put("categoryName", category1Name);
            jsonObject1.put("index", index);
            index++;

            //处理二级分类
            Map<Long, List<BaseCategoryView>> categoryMap2 = category2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));

            //创建二级分类处理的集合
            List<JSONObject> list2 = new ArrayList<>();

            //处理二分类
            for (Map.Entry<Long, List<BaseCategoryView>> entry2 : categoryMap2.entrySet()) {
                JSONObject jsonObject2 = new JSONObject();
                //获取二级分类的id
                Long category2Id = entry2.getKey();
                //获取二级分类的name
                List<BaseCategoryView> category3List = entry2.getValue();
                String category2Name = category3List.get(0).getCategory2Name();
                jsonObject2.put("categoryId", category2Id);
                jsonObject2.put("categoryName", category2Name);

                //创建三级集合
                List<JSONObject> list3 = new ArrayList<>();
                //处理三级分类
                for (BaseCategoryView baseCategoryView : category3List) {
                    JSONObject jsonObject3 = new JSONObject();
                    jsonObject3.put("categoryId", baseCategoryView.getCategory3Id());
                    jsonObject3.put("categoryName", baseCategoryView.getCategory3Name());
                    list3.add(jsonObject3);
                }
                jsonObject2.put("categoryChild", list3);

                //收集二级分类
                list2.add(jsonObject2);

            }

            jsonObject1.put("categoryChild", list2);

            list.add(jsonObject1);


        }

        return list;
    }


}
