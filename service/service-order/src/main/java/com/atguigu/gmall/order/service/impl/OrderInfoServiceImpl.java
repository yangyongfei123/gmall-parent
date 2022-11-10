package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo>
        implements OrderInfoService {
    @Resource
    private OrderDetailMapper orderDetailMapper;
    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Resource
    private RedisTemplate redisTemplate;
    @Value("${ware.url}")
    private String WARE_URL;

    @Override
    @Transactional
    public Long saveOrderInfo(OrderInfo orderInfo) {
        //计算总金额
        orderInfo.sumTotalAmount();
        //设置订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //订单交易编号（第三方支付用)
        String outTradeNo = "GBKL" + System.currentTimeMillis() + new Random().nextInt(1000);
        //设置订单交易编号
        orderInfo.setOutTradeNo(outTradeNo);
        //设置订单创建日期
        orderInfo.setCreateTime(new Date());
        //设置过期时间,间隔为1天
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());

        //设置订单流程状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        // 获取订单明细
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        //交易信息
        StringBuffer tradeBody = new StringBuffer();
        for (OrderDetail orderDetail : orderDetailList) {
            tradeBody.append(orderDetail.getSkuName() + " ");
        }
        if (tradeBody.toString().length() > 100) {
            orderInfo.setTradeBody(tradeBody.toString().substring(0, 100));
        } else {
            orderInfo.setTradeBody(tradeBody.toString());
        }

        orderInfoMapper.insert(orderInfo);

        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insert(orderDetail);
        }

        return orderInfo.getId();
    }

    @Override
    //设定流水号
    public String getTradeNo(Long userId) {
        // 定义key
        String tradeKey = "user:" + userId + ":tradeCode";
        // 定义一个流水号
        String tradeNo = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(tradeKey, tradeNo);

        return tradeNo;
    }

    @Override
    public Boolean checkTradeCode(Long userId, String tradeNo) {
        // 定义key
        String tradeNoKey = "user:" + userId + ":tradeCode";
        String redisTradeNo = (String) redisTemplate.opsForValue().get(tradeNoKey);
        boolean equals = redisTradeNo.equals(tradeNo);

        return equals;
    }

    @Override
    public void deleteTradeNo(Long userId) {
        // 定义key
        String tradeKey = "user:" + userId + ":tradeCode";
        redisTemplate.delete(tradeKey);
    }

    @Override
    public boolean checkStock(Long skuId, Integer skuNum) {
        // 远程调用http://localhost:9001/hasStock?skuId=10221&num=2
        String result = HttpClientUtil.doGet(WARE_URL + "hasStock?skuId=" + skuId + "&num=" + skuNum);

        return "1".equals(result);
    }

    @Override
    public IPage<OrderInfo> getMyOrder(Page<OrderInfo> pageParam, String userId) {
        IPage<OrderInfo> page = orderInfoMapper.selectPageByUserId(pageParam, userId);
        page.getRecords().stream().forEach(item -> {
            item.setOrderStatusName(OrderStatus.getStatusNameByStatus(item.getOrderStatus()));
        });


        return page;
    }


}