package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.ManagerService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.simpleframework.xml.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/admin/product")
public class SkuManageController {

    @Autowired
    private ManagerService managerService;



    /**
     * /admin/product/cancelSale/{skuId}
     * 下架
     * @param skuId
     * @return
     */
    @GetMapping("/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId){

        managerService.cancelSale(skuId);

        return Result.ok();
    }

    /**
     * /admin/product/onSale/{skuId}
     * 上架
     * @param skuId
     * @return
     */
    @GetMapping("/onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId){
        managerService.onSale(skuId);


        return Result.ok();
    }


    /**
     * /admin/product/list/{page}/{limit}
     * skuinfo分页查询
     * @return
     */
    @GetMapping("/list/{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit ){

        //封装对象
        Page<SkuInfo> skuInfoPage=new Page<>(page,limit);

        //调用service
       IPage<SkuInfo> skuInfoIPage= managerService.getSkuInfoByPage(skuInfoPage);

        return Result.ok(skuInfoIPage);
    }



    /**
     * /admin/product/saveSkuInfo
     * 保存skuInfo
     * @param skuInfo
     * @return
     */
    @PostMapping("/saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){


        managerService.saveSkuInfo(skuInfo);


        return Result.ok();

    }



    /**
     * 根据spuId 查询销售属性
     * admin/product/spuSaleAttrList/{spuId}
     * @param spuId
     * @return
     *
     *   * 参数数据接收：
     *      *  1.路径传值
     *      *   /url/{name}
     *      * @PathVariable
     *      * 2.普通传值
     *      *   /url?name=张三&age =18
     *      *    (String name ) {@RequestParam Person person} person->类中必须有一个属性叫name
     *      *    map:
     *      *     {@RequestParam Map paraMap}
     *      *
     *      * 3.json对象
     *      * @reqeustBody 对象
     *      *
     *      * 4.HttpServletRequest
     *      *  所有的数据都可以通过这个对象接收
     */
    @GetMapping("/spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable Long spuId ){

        List<SpuSaleAttr> spuSaleAttrList=managerService.spuSaleAttrList(spuId);

        return Result .ok(spuSaleAttrList);
    }



    /**
     * ///admin/product/spuImageList/{spuId}
     * 根据spuId 获取spuImage 集合
     * @param spuId
     * @return
     */
    @GetMapping("/spuImageList/{spuId}")
    public Result spuImageList(@PathVariable Long spuId){

        List<SpuImage> spuImageList=managerService.spuImageList(spuId);

        return Result.ok(spuImageList);

    }
}
