package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTradeMarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product/baseTrademark")
public class BaseTrademarkController {


    @Autowired
    private BaseTradeMarkService baseTradeMarkService;





    /**
     * /admin/product/baseTrademark/save
     * 保存品牌
     * @param baseTrademark
     * @return
     */
    @PostMapping("/save")
    public Result saveTradeMark(@RequestBody BaseTrademark baseTrademark){

        baseTradeMarkService.save(baseTrademark);
        return Result.ok();
    }


    /**
     * admin/product/baseTrademark/get/{id}
     * 根据id回显品牌
     * @param id
     * @return
     */
    @GetMapping("/get/{id}")
    public Result getTradeMarkById(@PathVariable Long id){
        BaseTrademark baseTrademark = baseTradeMarkService.getById(id);

        return Result.ok(baseTrademark);

    }



    /**
     * admin/product/baseTrademark/update
     * 修改品牌
     * @param baseTrademark
     * @return
     */
    @PutMapping("/update")
    public Result updateTradeMarkById(@RequestBody BaseTrademark baseTrademark){


        baseTradeMarkService.updateById(baseTrademark);
        return Result.ok();


    }


    /**
     * admin/product/baseTrademark/remove/{id}
     * 删除品牌
     * @param id
     * @return
     */
    @DeleteMapping("/remove/{id}")
    public Result deleteTrademarkById(@PathVariable Long id){

        baseTradeMarkService.removeById(id);

        return Result.ok();

    }



    /**
     * /admin/product/baseTrademark/{page}/{limit}
     * 分页查询品牌列表
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/{page}/{limit}")
    public Result getBaseTradeMarkByPage(@PathVariable Long page,
                                         @PathVariable Long limit){

        //封装分页对象
        Page<BaseTrademark> baseTrademarkPage=new Page<>(page,limit);

        //调用service
       IPage<BaseTrademark> baseTrademarkList=  baseTradeMarkService.getBaseTradeMarkByPage(baseTrademarkPage);


        return Result.ok(baseTrademarkList);
    }


}


