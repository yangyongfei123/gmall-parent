package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseTradeMarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("all")
public class BaseTradeMarkServiceImpl extends ServiceImpl<BaseTrademarkMapper,BaseTrademark> implements BaseTradeMarkService {

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;




    /**
     * 分页查询品牌列表
     * @param baseTrademarkPage
     * @return
     */
    @Override
    public IPage<BaseTrademark> getBaseTradeMarkByPage(Page<BaseTrademark> baseTrademarkPage) {

        //设置品牌排序
        QueryWrapper<BaseTrademark> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("id");


//        return baseMapper.selectPage(baseTrademarkPage,queryWrapper);

        return baseTrademarkMapper.selectPage(baseTrademarkPage,queryWrapper);
    }
}
