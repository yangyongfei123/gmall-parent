package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import lombok.SneakyThrows;
import net.bytebuddy.asm.Advice;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("all")
public class SearchServiceImpl implements SearchService {


    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ProductFeignClient productFeignClient;

    /**
     * 商品上架
     *
     * @param skuId
     */
    @Override
    public void upperGoods(Long skuId) {
        //创建goods对象
        Goods goods = new Goods();
        //查询skuInfo数据
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        goods.setId(skuId);
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setTitle(skuInfo.getSkuName());
        //查询实时价格
        goods.setPrice(productFeignClient.getSkuPrice(skuId).doubleValue());
        goods.setCreateTime(new Date());
        goods.setTmId(skuInfo.getTmId());

        //查询品牌数据对象
        BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
        goods.setTmName(trademark.getTmName());
        goods.setTmLogoUrl(trademark.getLogoUrl());

        //查询分类信息对象
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());

        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory3Name(categoryView.getCategory3Name());

        //查询平台属性集合
        List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);

        //转换平台属性
        List<SearchAttr> searchAttrs = attrList.stream().map(baseAttrInfo -> {
            SearchAttr searchAttr = new SearchAttr();
            searchAttr.setAttrId(baseAttrInfo.getId());
            searchAttr.setAttrName(baseAttrInfo.getAttrName());
            searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());

            return searchAttr;
        }).collect(Collectors.toList());
        goods.setAttrs(searchAttrs);

        //上架
        goodsRepository.save(goods);
    }

    /*8
    商品下架
     */
    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    /**
     * 商品热度排名统计
     *
     * @param skuId 加锁：效率低
     *              redis: incry by
     *              <p>
     *              hotScore   skuId:28   5
     *              skuId:27   1
     *              skuId:26   3
     *              选择redis： zset
     *              <p>
     *              需求：将热度记录在redis，当redis的热度统计每10次，去更新一次es
     */
    @Override
    public void incrHotScore(Long skuId) {

        //定义key
        String hotKey = "hotScore";

        //修改排名
        Double hotScore = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);
        //每10次修改es
        if (hotScore % 10 == 0) {

            //查询原来的数据
            Goods goods = goodsRepository.findById(skuId).get();
            goods.setHotScore(Math.round(hotScore));
            //修改es
            goodsRepository.save(goods);
        }


    }

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 商品搜索
     *
     * @param searchParam
     * @return 1.确定使用什么方式
     * <p>
     * ElasticSearchTemplate
     * ElasticSearchRepository
     * <p>
     * 选择 RestHighLevelClient
     */
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    @SneakyThrows
    public SearchResponseVo search(SearchParam searchParam) {

        //调用方法构建查询条件DSL
        SearchRequest searchRequest = buildQueryDSL(searchParam);
        //发送请求获取结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //转换查询返回的结果
        SearchResponseVo searchResponseVo = parseSearchResult(searchResponse);

        //设置当前页
        searchResponseVo.setPageNo(searchParam.getPageNo());
        //设置每页条数
        searchResponseVo.setPageSize(searchParam.getPageSize());
        //计算总页数  10  3
        if(searchResponseVo.getTotal()%searchParam.getPageSize()==0){

            searchResponseVo.setTotalPages(searchResponseVo.getTotal()/searchParam.getPageSize());

        }else{
            searchResponseVo.setTotalPages((searchResponseVo.getTotal()/searchParam.getPageSize())+1);

        }

        //计算总页数方式二： （总页数 +每页条数-1）/每页条数
//        long totalPages = (searchResponseVo.getTotal()+searchParam.getPageSize()-1)/searchParam.getPageSize();


        return searchResponseVo;
    }

    /**
     * 处理返回值结果集
     *
     * @param searchResponse
     * @return
     */
    private SearchResponseVo parseSearchResult(SearchResponse searchResponse) {

        //创建封装 对象
        SearchResponseVo searchResponseVo=new SearchResponseVo();


        //获取所有的聚合数据到map集合中
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
        //获取品牌数据
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");

        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map(bucket -> {
            //创建封装对象
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //获取品牌id
            long tmId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
            searchResponseTmVo.setTmId(tmId);

            //获取品牌聚合的自聚合
            Map<String, Aggregation> subAggregationMap = ((Terms.Bucket) bucket).getAggregations().asMap();
            //获取品牌名称
            ParsedStringTerms tmNameAgg = (ParsedStringTerms) subAggregationMap.get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);
            //获取品牌logo
            ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) subAggregationMap.get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);

            return searchResponseTmVo;
        }).collect(Collectors.toList());

        //设置品牌列表数据到响应对象中
        searchResponseVo.setTrademarkList(trademarkList);

        //处理平台属性
        ParsedNested attrsAgg = (ParsedNested) aggregationMap.get("attrsAgg");
        //获取
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");
        //遍历数据--封装品台属性集合
        List<SearchResponseAttrVo> searchResponseAttrVoList = attrIdAgg.getBuckets().stream().map(bucket -> {

            //创建封装对象
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            //获取平台属性id
            long attrId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
            searchResponseAttrVo.setAttrId(attrId);
            //获取平台属性所有的自聚合
            Map<String, Aggregation> subAggrations = ((Terms.Bucket) bucket).getAggregations().getAsMap();

            //获取平台属性名
            ParsedStringTerms attrNameAgg = (ParsedStringTerms) subAggrations.get("attrNameAgg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseAttrVo.setAttrName(attrName);

            //获取平台属性值
            ParsedStringTerms attrValueAgg = (ParsedStringTerms) subAggrations.get("attrValueAgg");
            List<String> attrValueList = attrValueAgg.getBuckets().stream().map(attrValueBucket -> {
                String attrValue = ((Terms.Bucket) attrValueBucket).getKeyAsString();

                return attrValue;
            }).collect(Collectors.toList());

            searchResponseAttrVo.setAttrValueList(attrValueList);


            return searchResponseAttrVo;
        }).collect(Collectors.toList());

        //设置平台属性集合到响应对象中
        searchResponseVo.setAttrsList(searchResponseAttrVoList);


        //封装商品信息
        SearchHits hits = searchResponse.getHits();
        //获取商品列表数据的hits
        SearchHit[] hitsGoods = hits.getHits();

        //处理封装成List<Goods>
        List<Goods> listGoods = Arrays.stream(hitsGoods).map(hit -> {

            //获取数据
            Goods goods = JSONObject.parseObject(hit.getSourceAsString(), Goods.class);

            //获取
            if (hit.getHighlightFields().get("title") != null) {

                //获取高亮的数据
                String title = hit.getHighlightFields().get("title").getFragments()[0].toString();
                goods.setTitle(title);

            }


            return goods;
        }).collect(Collectors.toList());

        //封装商品列表数据
        searchResponseVo.setGoodsList(listGoods);


        //获取总记录数
        long total = hits.getTotalHits().value;
        searchResponseVo.setTotal(total);

        return searchResponseVo;
    }

    /**
     * 构建查询条件
     *
     * @return
     */
    private SearchRequest buildQueryDSL(SearchParam searchParam) {
        //搜索封装 的请求对象
        SearchRequest searchRequest = new SearchRequest("goods");
        //封装的条件对象构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //封装查询条件
        //创建一个多条件对象
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //根据关键字封装条件
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            //创建匹配关键字的对象
            MatchQueryBuilder title = QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND);

            boolQuery.must(title);

        }
        //构建品牌查询 trademark 2:华为
        //2:华为
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = trademark.split(":");
            //判断
            if (split != null && split.length == 2) {
                boolQuery.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }

        //判断分类
        if (null != searchParam.getCategory3Id()) {


            boolQuery.filter(QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id()));

        }
        if (null != searchParam.getCategory2Id()) {


            boolQuery.filter(QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id()));

        }
        if (null != searchParam.getCategory1Id()) {


            boolQuery.filter(QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id()));

        }

        //平台属性
        String[] props = searchParam.getProps();
        //判断
        if (props != null && props.length > 0) {

            //&props=23:8G:运行内存&props=24:128G:机身内存
            for (String prop : props) {
                //prop: 23:8G:运行内存
                String[] split = prop.split(":");
                //判断
                if (split != null && split.length == 3) {
                    //构建嵌套多条件对象
                    BoolQueryBuilder boolQueryProp = QueryBuilders.boolQuery();
                    //创建子嵌套对象
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();

                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));

                    boolQueryProp.must(QueryBuilders.nestedQuery("attrs", subBoolQuery, ScoreMode.None));
                    boolQuery.filter(boolQueryProp);

                }

            }


        }

        //将条件添加到构造器
        searchSourceBuilder.query(boolQuery);

        //排序
//        searchSourceBuilder.sort("price", SortOrder.DESC);
        //获取参数 1:asc或者1：desc  2:asc或者2:desc
        //1:热度排序  2：价格排序
        String order = searchParam.getOrder();
        //判断
        if (!StringUtils.isEmpty(order)) {
            //1:asc
            String[] split = order.split(":");

            //定义字段判断赋值
            String field = null;
            switch (split[0]) {

                case "1":
                    field = "hotScore";
                    break;
                case "2":
                    field="price";
                    break;
            }
            searchSourceBuilder.sort(field, "desc".equals(split[1])?SortOrder.DESC:SortOrder.ASC);
        } else {
            //默认排序方式--热度

            searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        }

        //高亮
        //创建高亮构建对象
        HighlightBuilder highlightBuilder=new HighlightBuilder();
        //前缀
        highlightBuilder.preTags("<span style='color:red'>");
        //后缀
        highlightBuilder.postTags("</span>");
        //指定字段
        highlightBuilder.field("title");


        searchSourceBuilder.highlighter(highlightBuilder);

        //聚合--品牌聚合
        //创建品牌集合构建对象
        TermsAggregationBuilder tmIdAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId");

        //添加子聚合
        tmIdAggregationBuilder.subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                              .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        searchSourceBuilder.aggregation(tmIdAggregationBuilder);

        //聚合--平台属性聚合nested


        searchSourceBuilder.aggregation( AggregationBuilders.nested("attrsAgg","attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId").
                        subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

        //分页 开始索引=（当前页-1）*每页条数
        int index=(searchParam.getPageNo()-1)*searchParam.getPageSize();
        searchSourceBuilder.from(index);
        searchSourceBuilder.size(searchParam.getPageSize());


        //结果处理
        searchSourceBuilder.fetchSource(new String[]{"id","defaultImg","title","price"},null);

        System.out.println("构建的DSL语句\t"+searchSourceBuilder.toString());

        //将条件添加到请求对象
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
}
