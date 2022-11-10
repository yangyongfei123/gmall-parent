package com.atguigu.gmall.list.receiver;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.list.service.SearchService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ListReceiver {
    @Autowired
    private SearchService searchService;

    //  开启消息监听 监听商品上架！
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(value = @Queue(
            value = MqConst.QUEUE_GOODS_UPPER, durable = "true", autoDelete = "false"
    ), exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
            key = {MqConst.ROUTING_GOODS_UPPER}
    ))
    public void upperGoodsToEs(Long skuId, Message message, Channel channel) {
        try {
            //  获取到skuId,并判断
            if (skuId != null) {
                //  则调用商品上架的方法！
                searchService.upperGoods(skuId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//  确认消费者消费消息！
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


    //  开启消息监听 监听商品下架！
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_LOWER, durable = "true", autoDelete = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
            key = {MqConst.ROUTING_GOODS_LOWER}
    ))
    public void lowerGoodsToEs(Long skuId, Message message, Channel channel) {
        try {
            if (skuId != null) {
                searchService.lowerGoods(skuId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
