package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private StringRedisTemplate redisTemplate;

//    /**
//     * 测试本地锁的局限性
//     */
//    @Override
//    public synchronized void  testLock() {
//
//        //读取数据
//        String num = redisTemplate.opsForValue().get("num");
//
//        //转换
//        int number = Integer.parseInt(num);
//
//
//        //递增
//     redisTemplate.opsForValue().set("num",String.valueOf(++number));
//
//
//
//    }
//    /**
//     * 测试redis实现分布式锁
//     */
//    @Override
//    public  void  testLock() {
//
//        //生成唯一标识
//        String uuId = UUID.randomUUID().toString().replaceAll("-", "");
//
//        //setnx lock
////        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", "666",7,TimeUnit.SECONDS);
//        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuId,7,TimeUnit.SECONDS);
//        //判断 添加成功 true, 失败false
//        //设置过期时间
////        redisTemplate.expire("lock",10, TimeUnit.SECONDS);
//        if(lock){
//            //读取数据
//            String num = redisTemplate.opsForValue().get("num");
//            //转换
//            int number = Integer.parseInt(num);
//            //递增
//            redisTemplate.opsForValue().set("num",String.valueOf(++number));
//
//
//            //定义lua脚本
//            String script="if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
//                    "then\n" +
//                    "    return redis.call(\"del\",KEYS[1])\n" +
//                    "else\n" +
//                    "    return 0\n" +
//                    "end";
//
//
//            //设置lua脚本
//            DefaultRedisScript<Long> redisScript=new DefaultRedisScript<>();
//            redisScript.setScriptText(script);
//            redisScript.setResultType(Long.class);
//            //执行lua脚本
//            redisTemplate.execute(redisScript, Arrays.asList("lock"),uuId);
//
////            //判断
////            if(uuId.equals(redisTemplate.opsForValue().get("lock"))){
////
////                //释放锁
////                redisTemplate.delete("lock");
////            }
//
//
//        }else{
//            //没有到锁的线程
//            try {
//                Thread.sleep(200);
//                //自旋
//                this.testLock();
//
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//
//        }
//
//
//    }

    @Autowired
    private RedissonClient redissonClient;

        /**
     * 测试Redisson分布式锁实现
         * 1.加锁
         *  RLock rlock=redisson.getLock("lock");
         *  rlock.lock();
         * 2.释放锁
         * rlock.unlock();
         *
         *
     */
    @Override
    public  void  testLock() {
        try {
            //定义key
            String key ="sku:"+28+":lock";
            //获取锁
            RLock lock = redissonClient.getLock(key);
            //加锁
//        lock.lock();
            //设置锁的超时时间
//        lock.lock(10,TimeUnit.SECONDS);
            //设置最大等待时间
            boolean result = lock.tryLock(100, 10, TimeUnit.SECONDS);

            if(result){

                try {
                    //读取数据
                    String num = redisTemplate.opsForValue().get("num");

                    //转换
                    int number = Integer.parseInt(num);


                    //递增
                    redisTemplate.opsForValue().set("num",String.valueOf(++number));

                    this.test(number);

                } finally {
                    lock.unlock();
                }

            }else{

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.testLock();

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    /**
     * 读取数据
     * @return
     */
    @Override
    public String read() {
        //获取读写锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readWriteLock");
        //获取读锁
        RLock rLock = readWriteLock.readLock();
        //加锁
        rLock.lock(10,TimeUnit.SECONDS);

        String msg = redisTemplate.opsForValue().get("msg");

//        rLock.unlock();

        return "读取的数据\t"+msg;
    }

    /**
     * 写入数据
     * @return
     */
    @Override
    public String write() {
        //获取读写锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readWriteLock");
        RLock rLock = readWriteLock.writeLock();
        rLock.lock(10,TimeUnit.SECONDS);

        String msg = UUID.randomUUID().toString().replaceAll("-", "");

        redisTemplate.opsForValue().set("msg",msg);

//        rLock.unlock();
        return "写入的数据是\t"+msg;
    }

    private void test(int number) {
        String key ="sku:"+28+":lock";
        RLock lock = redissonClient.getLock(key);

        lock.lock();

        System.out.println(number);
        lock.unlock();


    }


}
