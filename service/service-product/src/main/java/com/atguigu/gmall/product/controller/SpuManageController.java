package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.ManagerService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product")
public class SpuManageController {


    @Autowired
    private ManagerService managerService;




    /**
     * admin/product/saveSpuInfo
     * 保存spu
     * @param spuInfo
     * @return
     */
    @PostMapping("/saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        managerService.saveSpuInfo(spuInfo);

        return Result.ok();
    }

    /**
     * admin/product/baseSaleAttrList
     * 获取销售属性
     * @return
     */
    @GetMapping("/baseSaleAttrList")
    public Result baseSaleAttrList(){


        List<BaseSaleAttr> baseSaleAttrList=managerService.baseSaleAttrList();

        return Result.ok(baseSaleAttrList);
    }


    /**
     * /admin/product/{page}/{limit}
     * 分页查询spu列表
     * @param page
     * @param limit
     * @param spuInfo
     * @return
     */
    @GetMapping("/{page}/{limit}")
    public Result getSpuInfoByPage(@PathVariable Long page,
                                   @PathVariable Long limit,
                                   SpuInfo spuInfo){

        //封装分页查询对象
        Page<SpuInfo> spuInfoPage=new Page<>(page,limit);
       IPage<SpuInfo> infoIPage= managerService.getSpuInfoByPage(spuInfoPage,spuInfo);

        return Result.ok(infoIPage);

    }


}
