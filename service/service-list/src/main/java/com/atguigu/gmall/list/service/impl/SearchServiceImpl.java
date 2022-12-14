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
     * ????????????
     *
     * @param skuId
     */
    @Override
    public void upperGoods(Long skuId) {
        //??????goods??????
        Goods goods = new Goods();
        //??????skuInfo??????
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        goods.setId(skuId);
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setTitle(skuInfo.getSkuName());
        //??????????????????
        goods.setPrice(productFeignClient.getSkuPrice(skuId).doubleValue());
        goods.setCreateTime(new Date());
        goods.setTmId(skuInfo.getTmId());

        //????????????????????????
        BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
        goods.setTmName(trademark.getTmName());
        goods.setTmLogoUrl(trademark.getLogoUrl());

        //????????????????????????
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());

        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory3Name(categoryView.getCategory3Name());

        //????????????????????????
        List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);

        //??????????????????
        List<SearchAttr> searchAttrs = attrList.stream().map(baseAttrInfo -> {
            SearchAttr searchAttr = new SearchAttr();
            searchAttr.setAttrId(baseAttrInfo.getId());
            searchAttr.setAttrName(baseAttrInfo.getAttrName());
            searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());

            return searchAttr;
        }).collect(Collectors.toList());
        goods.setAttrs(searchAttrs);

        //??????
        goodsRepository.save(goods);
    }

    /*8
    ????????????
     */
    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    /**
     * ????????????????????????
     *
     * @param skuId ??????????????????
     *              redis: incry by
     *              <p>
     *              hotScore   skuId:28   5
     *              skuId:27   1
     *              skuId:26   3
     *              ??????redis??? zset
     *              <p>
     *              ???????????????????????????redis??????redis??????????????????10?????????????????????es
     */
    @Override
    public void incrHotScore(Long skuId) {

        //??????key
        String hotKey = "hotScore";

        //????????????
        Double hotScore = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);
        //???10?????????es
        if (hotScore % 10 == 0) {

            //?????????????????????
            Goods goods = goodsRepository.findById(skuId).get();
            goods.setHotScore(Math.round(hotScore));
            //??????es
            goodsRepository.save(goods);
        }


    }

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * ????????????
     *
     * @param searchParam
     * @return 1.????????????????????????
     * <p>
     * ElasticSearchTemplate
     * ElasticSearchRepository
     * <p>
     * ?????? RestHighLevelClient
     */
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    @SneakyThrows
    public SearchResponseVo search(SearchParam searchParam) {

        //??????????????????????????????DSL
        SearchRequest searchRequest = buildQueryDSL(searchParam);
        //????????????????????????
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //???????????????????????????
        SearchResponseVo searchResponseVo = parseSearchResult(searchResponse);

        //???????????????
        searchResponseVo.setPageNo(searchParam.getPageNo());
        //??????????????????
        searchResponseVo.setPageSize(searchParam.getPageSize());
        //???????????????  10  3
        if(searchResponseVo.getTotal()%searchParam.getPageSize()==0){

            searchResponseVo.setTotalPages(searchResponseVo.getTotal()/searchParam.getPageSize());

        }else{
            searchResponseVo.setTotalPages((searchResponseVo.getTotal()/searchParam.getPageSize())+1);

        }

        //??????????????????????????? ???????????? +????????????-1???/????????????
//        long totalPages = (searchResponseVo.getTotal()+searchParam.getPageSize()-1)/searchParam.getPageSize();


        return searchResponseVo;
    }

    /**
     * ????????????????????????
     *
     * @param searchResponse
     * @return
     */
    private SearchResponseVo parseSearchResult(SearchResponse searchResponse) {

        //???????????? ??????
        SearchResponseVo searchResponseVo=new SearchResponseVo();


        //??????????????????????????????map?????????
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
        //??????????????????
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");

        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map(bucket -> {
            //??????????????????
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //????????????id
            long tmId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
            searchResponseTmVo.setTmId(tmId);

            //??????????????????????????????
            Map<String, Aggregation> subAggregationMap = ((Terms.Bucket) bucket).getAggregations().asMap();
            //??????????????????
            ParsedStringTerms tmNameAgg = (ParsedStringTerms) subAggregationMap.get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);
            //????????????logo
            ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) subAggregationMap.get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);

            return searchResponseTmVo;
        }).collect(Collectors.toList());

        //??????????????????????????????????????????
        searchResponseVo.setTrademarkList(trademarkList);

        //??????????????????
        ParsedNested attrsAgg = (ParsedNested) aggregationMap.get("attrsAgg");
        //??????
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");
        //????????????--????????????????????????
        List<SearchResponseAttrVo> searchResponseAttrVoList = attrIdAgg.getBuckets().stream().map(bucket -> {

            //??????????????????
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            //??????????????????id
            long attrId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
            searchResponseAttrVo.setAttrId(attrId);
            //????????????????????????????????????
            Map<String, Aggregation> subAggrations = ((Terms.Bucket) bucket).getAggregations().getAsMap();

            //?????????????????????
            ParsedStringTerms attrNameAgg = (ParsedStringTerms) subAggrations.get("attrNameAgg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseAttrVo.setAttrName(attrName);

            //?????????????????????
            ParsedStringTerms attrValueAgg = (ParsedStringTerms) subAggrations.get("attrValueAgg");
            List<String> attrValueList = attrValueAgg.getBuckets().stream().map(attrValueBucket -> {
                String attrValue = ((Terms.Bucket) attrValueBucket).getKeyAsString();

                return attrValue;
            }).collect(Collectors.toList());

            searchResponseAttrVo.setAttrValueList(attrValueList);


            return searchResponseAttrVo;
        }).collect(Collectors.toList());

        //??????????????????????????????????????????
        searchResponseVo.setAttrsList(searchResponseAttrVoList);


        //??????????????????
        SearchHits hits = searchResponse.getHits();
        //???????????????????????????hits
        SearchHit[] hitsGoods = hits.getHits();

        //???????????????List<Goods>
        List<Goods> listGoods = Arrays.stream(hitsGoods).map(hit -> {

            //????????????
            Goods goods = JSONObject.parseObject(hit.getSourceAsString(), Goods.class);

            //??????
            if (hit.getHighlightFields().get("title") != null) {

                //?????????????????????
                String title = hit.getHighlightFields().get("title").getFragments()[0].toString();
                goods.setTitle(title);

            }


            return goods;
        }).collect(Collectors.toList());

        //????????????????????????
        searchResponseVo.setGoodsList(listGoods);


        //??????????????????
        long total = hits.getTotalHits().value;
        searchResponseVo.setTotal(total);

        return searchResponseVo;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    private SearchRequest buildQueryDSL(SearchParam searchParam) {
        //???????????? ???????????????
        SearchRequest searchRequest = new SearchRequest("goods");
        //??????????????????????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //??????????????????
        //???????????????????????????
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //???????????????????????????
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            //??????????????????????????????
            MatchQueryBuilder title = QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND);

            boolQuery.must(title);

        }
        //?????????????????? trademark 2:??????
        //2:??????
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = trademark.split(":");
            //??????
            if (split != null && split.length == 2) {
                boolQuery.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }

        //????????????
        if (null != searchParam.getCategory3Id()) {


            boolQuery.filter(QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id()));

        }
        if (null != searchParam.getCategory2Id()) {


            boolQuery.filter(QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id()));

        }
        if (null != searchParam.getCategory1Id()) {


            boolQuery.filter(QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id()));

        }

        //????????????
        String[] props = searchParam.getProps();
        //??????
        if (props != null && props.length > 0) {

            //&props=23:8G:????????????&props=24:128G:????????????
            for (String prop : props) {
                //prop: 23:8G:????????????
                String[] split = prop.split(":");
                //??????
                if (split != null && split.length == 3) {
                    //???????????????????????????
                    BoolQueryBuilder boolQueryProp = QueryBuilders.boolQuery();
                    //?????????????????????
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();

                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));

                    boolQueryProp.must(QueryBuilders.nestedQuery("attrs", subBoolQuery, ScoreMode.None));
                    boolQuery.filter(boolQueryProp);

                }

            }


        }

        //???????????????????????????
        searchSourceBuilder.query(boolQuery);

        //??????
//        searchSourceBuilder.sort("price", SortOrder.DESC);
        //???????????? 1:asc??????1???desc  2:asc??????2:desc
        //1:????????????  2???????????????
        String order = searchParam.getOrder();
        //??????
        if (!StringUtils.isEmpty(order)) {
            //1:asc
            String[] split = order.split(":");

            //????????????????????????
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
            //??????????????????--??????

            searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        }

        //??????
        //????????????????????????
        HighlightBuilder highlightBuilder=new HighlightBuilder();
        //??????
        highlightBuilder.preTags("<span style='color:red'>");
        //??????
        highlightBuilder.postTags("</span>");
        //????????????
        highlightBuilder.field("title");


        searchSourceBuilder.highlighter(highlightBuilder);

        //??????--????????????
        //??????????????????????????????
        TermsAggregationBuilder tmIdAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId");

        //???????????????
        tmIdAggregationBuilder.subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                              .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        searchSourceBuilder.aggregation(tmIdAggregationBuilder);

        //??????--??????????????????nested


        searchSourceBuilder.aggregation( AggregationBuilders.nested("attrsAgg","attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId").
                        subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

        //?????? ????????????=????????????-1???*????????????
        int index=(searchParam.getPageNo()-1)*searchParam.getPageSize();
        searchSourceBuilder.from(index);
        searchSourceBuilder.size(searchParam.getPageSize());


        //????????????
        searchSourceBuilder.fetchSource(new String[]{"id","defaultImg","title","price"},null);

        System.out.println("?????????DSL??????\t"+searchSourceBuilder.toString());

        //??????????????????????????????
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
}
