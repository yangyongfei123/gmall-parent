package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Controller
@SuppressWarnings("all")
public class IndexController {


    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 首页显示-渲染的方式
     * @return
     */
    @GetMapping({"/","index.html"})
    public String toIndex(Model model){
        //调用service-product获取三级分类数据
        Result<List> result = productFeignClient.getBaseCategoryList();

        model.addAttribute("list",result.getData());

        return "index/index";
    }


    /**
     * 首页显示-生成静态页面，nginx部署
     * @return
     */
    @GetMapping("/createIndex")
    @ResponseBody
    public Result createIndex(){
        //调用service-product获取三级分类数据
        Result<List> result = productFeignClient.getBaseCategoryList();


        //创建上下文对象
        Context  context=new Context();
        context.setVariable("list",result.getData());

        //创建输出流
        FileWriter writer= null;
        try {
            writer = new FileWriter("D:\\gmall\\index.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //生成页面
        templateEngine.process("index/index",context,writer);
        return Result.ok("页面生成成功！");
    }



}
