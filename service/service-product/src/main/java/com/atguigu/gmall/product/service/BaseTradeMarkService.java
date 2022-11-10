package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BaseTradeMarkService extends IService<BaseTrademark> {

    /**
     *  分页查询品牌列表
     * @param baseTrademarkPage
     * @return
     */
    IPage<BaseTrademark> getBaseTradeMarkByPage(Page<BaseTrademark> baseTrademarkPage);

}
