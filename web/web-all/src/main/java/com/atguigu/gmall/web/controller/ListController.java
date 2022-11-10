package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@SuppressWarnings("all")
public class ListController {


    @Autowired
    private ListFeignClient listFeignClient;

    /**
     * 跳转商品检索列表页面
     * @return
     */
    @GetMapping("/list.html")
    public String toList(SearchParam searchParam, Model model){

        //返回参数雷彪
        model.addAttribute("searchParam",searchParam);
        //获取检索数据
        Result<Map> result = listFeignClient.search(searchParam);
        model.addAllAttributes(result.getData());

        //记录拼接url
        String urlParam=makeUrlParam(searchParam);
        model.addAttribute("urlParam",urlParam);

        //处理品牌面包屑 品牌：小米
        String trademarkParam=makeTrademarkParam(searchParam.getTrademark());
        model.addAttribute("trademarkParam",trademarkParam);

        //处理平台属性面包屑

        List<Map> propsParamList=makePropsParamList(searchParam.getProps());
        model.addAttribute("propsParamList",propsParamList);

        //处理排序
        Map<String,String> orderMap=makeOrderMAP(searchParam.getOrder());
        model.addAttribute("orderMap",orderMap);
        return "list/index";
    }

    private Map<String, String> makeOrderMAP(String order) {

        Map<String,String> orderMap=new HashMap<>();

         if(!StringUtils.isEmpty(order)){

             String[] split = order.split(":");
             //判断
             if(split!=null &&split.length==2){
                 orderMap.put("type",split[0]);
                 orderMap.put("sort",split[1]);
             }
         }else{
             //设置默认排序
             orderMap.put("type","1");
             orderMap.put("sort","desc");
         }
        return orderMap;
    }

    /**
     * 处理平台属性
     * @param props
     * @return
     */
    private List<Map> makePropsParamList(String[] props) {
        List<Map> mapList=new ArrayList<>();
        //判断
        if(props!=null &&props.length>0){
            for (String prop : props) {
                //prop  23:6G:运行内存      运行内存:6G
                String[] split = prop.split(":");
                //判断
                if(split!=null &&split.length==3){
                    Map map=new HashMap();
                    map.put("attrName",split[2]);
                    map.put("attrValue",split[1]);
                    map.put("attrId",split[0]);

                    mapList.add(map);
                }

            }

        }

        return mapList;
    }

    /**
     * 处理品牌面包屑
     * @param trademark
     * @return
     */
    private String makeTrademarkParam(String trademark) {
        //判断 1:小米
        if(!StringUtils.isEmpty(trademark)){
            //截取
            String[] split = trademark.split(":");
            if(split!=null &&split.length==2){
                return "品牌:"+split[1];
            }
        }

        return "";
    }

    /**
     * 拼接urlParam
     * @param searchParam
     * @return
     */
    private String makeUrlParam(SearchParam searchParam) {
        StringBuilder builder=new StringBuilder();
        //关键字
        String keyword = searchParam.getKeyword();
        if(!StringUtils.isEmpty(keyword)){

            builder.append("keyword=").append(keyword);

        }

        //分类查询
        if(searchParam.getCategory1Id()!=null){

            builder.append("category1Id=").append(searchParam.getCategory1Id());
        }

        if(searchParam.getCategory2Id()!=null){

            builder.append("category2Id=").append(searchParam.getCategory2Id());
        }

        if(searchParam.getCategory3Id()!=null){

            builder.append("category3Id=").append(searchParam.getCategory3Id());
        }

        //品牌
        String trademark = searchParam.getTrademark();
        if(!StringUtils.isEmpty(trademark)){

            if(builder.length()>0){

                builder.append("&trademark=").append(trademark);
            }
        }


        //平台属性
        String[] props = searchParam.getProps();
        if(props!=null &&props.length>0){

            for (String prop : props) {
                if(builder.length()>0){

                    builder.append("&props=").append(prop);

                }

            }


        }



        return "list.html?"+builder.toString();
    }
}
