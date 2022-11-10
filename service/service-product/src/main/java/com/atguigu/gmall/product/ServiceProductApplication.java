package com.atguigu.gmall.product;

import com.atguigu.gmall.common.constant.RedisConst;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.atguigu.gmall")
//public class ServiceProductApplication implements CommandLineRunner {
public class ServiceProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceProductApplication.class,args);
    }


    @Autowired
    private RedissonClient redissonClient;
//    /**
//     * 初始化布隆过滤器
//     * @param args
//     * @throws Exception
//     */
//    @Override
//    public void run(String... args) throws Exception {
//
//        //获取布隆过滤器
//        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
//        //初始化 误判率   数据规模
//        bloomFilter.tryInit(100000,0.01);
//    }
}
