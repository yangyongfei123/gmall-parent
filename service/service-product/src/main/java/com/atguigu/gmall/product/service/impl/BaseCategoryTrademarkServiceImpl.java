package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.product.mapper.BaseCategoryTrademarkMapper;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("all")
public class BaseCategoryTrademarkServiceImpl extends ServiceImpl<BaseCategoryTrademarkMapper,BaseCategoryTrademark> implements BaseCategoryTrademarkService {


    @Autowired
    private BaseCategoryTrademarkMapper baseCategoryTrademarkMapper;

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;



    /**
     * 查询分类下关联的品牌列表
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseTrademark> findTrademarkList(Long category3Id) {

        //根据选中的分类查询中间表对应的品牌id
        //封装条件
        QueryWrapper<BaseCategoryTrademark> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("category3_id",category3Id);

        //执行查询
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(queryWrapper);

        //创建集合收集品牌id
        List<Long> trademarkIds = null;


        //判断
        if(!CollectionUtils.isEmpty(baseCategoryTrademarkList)){

            //Stream
            // map: 处理后的结果类型和处理前的类型不一致
            // filter：处理前后类型一致
          trademarkIds = baseCategoryTrademarkList.stream().map(baseCategoryTrademark -> {


                return baseCategoryTrademark.getTrademarkId();

            }).collect(Collectors.toList());


//            //增强for遍历
//            for (BaseCategoryTrademark baseCategoryTrademark : baseCategoryTrademarkList) {
//
//                trademarkIds.add(baseCategoryTrademark.getTrademarkId());
//            }
//

            List<BaseTrademark> baseTrademarkList = baseTrademarkMapper.selectBatchIds(trademarkIds);

            return baseTrademarkList;

        }
        //查询品牌对象



        return null;
    }

    /**
     *
     * 根据category3Id获取可选品牌列表
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseTrademark> findCurrentTrademarkList(Long category3Id) {
        //查询当前分类关联的品牌
        //根据选中的分类查询中间表对应的品牌id
        //封装条件
        QueryWrapper<BaseCategoryTrademark> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("category3_id",category3Id);

        //执行查询
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(queryWrapper);



        //Stream
        if(!CollectionUtils.isEmpty(baseCategoryTrademarkList)){

            List<Long> trademarkIds = baseCategoryTrademarkList.stream().map(baseCategoryTrademark -> {


                return baseCategoryTrademark.getTrademarkId();
            }).collect(Collectors.toList());

            //条件查询品牌
            QueryWrapper<BaseTrademark> queryWrapper1=new QueryWrapper<>();

            queryWrapper1.notIn("id",trademarkIds);
            List<BaseTrademark> baseTrademarkList = baseTrademarkMapper.selectList(queryWrapper1);

            return baseTrademarkList;


//            //查询所有的品牌
//            List<BaseTrademark> baseTrademarkList = baseTrademarkMapper.selectList(null);
//
//            //Stream
//            List<BaseTrademark> baseTrademarks = baseTrademarkList.stream().filter(baseTrademark -> {
//
//
//                return !trademarkIds.contains(baseTrademark.getId());
//            }).collect(Collectors.toList());
//
//            return baseTrademarks;


        }

        return baseTrademarkMapper.selectList(null);
    }

    /**
     * 保存分类品牌关联
     * @param categoryTrademarkVo
     */
    @Override
    public void save(CategoryTrademarkVo categoryTrademarkVo) {

        //获取品牌id集合
        List<Long> trademarkIdList = categoryTrademarkVo.getTrademarkIdList();
        //判断
        if(!CollectionUtils.isEmpty(trademarkIdList)){

            List<BaseCategoryTrademark> baseCategoryTrademarks = trademarkIdList.stream().map(trademarkId -> {
                //创建对象
                BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
                baseCategoryTrademark.setTrademarkId(trademarkId);
                baseCategoryTrademark.setCategory3Id(categoryTrademarkVo.getCategory3Id());

                return baseCategoryTrademark;
            }).collect(Collectors.toList());
            this.saveBatch(baseCategoryTrademarks);
//            super.saveBatch(baseCategoryTrademarks);
        }



        //保存
//        baseCategoryTrademarkMapper.insert()

    }

    /**
     *  删除分类关联的品牌
     * @param category3Id
     * @param trademarkId
     */
    @Override
    public void removeCategoryTrademark(Long category3Id, Long trademarkId) {

        QueryWrapper<BaseCategoryTrademark> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("category3_id",category3Id);
        queryWrapper.eq("trademark_id",trademarkId);

        baseCategoryTrademarkMapper.delete(queryWrapper);

    }
}
