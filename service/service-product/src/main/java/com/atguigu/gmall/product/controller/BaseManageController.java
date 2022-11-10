package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Api(tags = "商品后台管理接口")
@RestController
@RequestMapping("/admin/product")
//@CrossOrigin
public class BaseManageController {


    @Autowired
    private ManagerService managerService;




    /**
     * admin/product/getAttrValueList/{attrId}
     * 回显平台属性值集合
     * @param attrId
     * @return
     */
    @GetMapping("/getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable Long attrId ){


      List<BaseAttrValue> baseAttrValueList=  managerService.getAttrValueList(attrId);

        return Result.ok(baseAttrValueList);
    }


    /**
     * admin/product/saveAttrInfo
     * 保存-修改平台属性
     * @return
     */
    @PostMapping("/saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){

        managerService.saveAttrInfo(baseAttrInfo);


        return Result.ok();
    }



    /**
     * 根据分类Id 获取平台属性集合
     * admin/product/attrInfoList/{category1Id}/{category2Id}/{category3Id}
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @GetMapping("/attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(@PathVariable Long category1Id,
                               @PathVariable Long category2Id,
                               @PathVariable Long category3Id){

        List<BaseAttrInfo> baseAttrInfoList=managerService.attrInfoList(category1Id,category2Id,category3Id);


        return Result.ok(baseAttrInfoList);
    }

    /**
     * 根据一级分类id查询二级分类列表
     * /admin/product/getCategory2/{category1Id}
     * @param category1Id
     * @return
     */
    @GetMapping("/getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id ){

       List<BaseCategory2> baseCategory2List= managerService.getCategory2(category1Id);

        return Result.ok(baseCategory2List);
    }


    /**
     * 根据二级分类id查询三级分类列表
     * /admin/product/getCategory3/{category2Id}
     * @param category2Id
     * @return
     */
    @GetMapping("/getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id ){



        List<BaseCategory3> baseCategory3List= managerService.getCategory3(category2Id);

        return Result.ok(baseCategory3List);
    }



    /**
     * 查询一级分类列表
     * @return
     */
    @GetMapping("/getCategory1")
    @ApiOperation(value = "查询一级分类列表")
    public Result getCategory1(){

       List<BaseCategory1> baseCategory1List= managerService.getCategory1();

        return Result.ok(baseCategory1List);
    }






}
