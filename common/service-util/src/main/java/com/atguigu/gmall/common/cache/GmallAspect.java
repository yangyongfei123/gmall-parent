package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;
    /**
     * 分布式锁+缓存
     *
     * 思路：
     *  1.定义数据key
     *
     *
     *  2.获取数据
     *      有，直接返回
     *      没有，
     *     3.获取锁key,获取锁
     *     没有，自旋
     *      有：获取数据库数据
     *       4.有，缓存，返回
     *       没有，空对象，返回
     *
     *       5.释放锁
     *
     *       6.兜底--查询数据库
     *
     *
     *
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
       Object object=new Object();

       //定义数据库 例如：sku:28:info
       //获取方法的签名对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取注解
        GmallCache annotation = signature.getMethod().getAnnotation(GmallCache.class);
        //获取前缀
        String prefix = annotation.prefix();
        //获取后缀
        String suffix = annotation.suffix();
        //获取参数列表
        Object[] args = joinPoint.getArgs();
        //拼接key
        String dataKey=prefix+ Arrays.toString(args)+suffix;
        //获取当前切到的方法返回值类型
        Class aClass = signature.getReturnType();

        try {
            //尝试获取数据
            object=this.cacheHit(dataKey,aClass);

            //判断是否从缓存中获取到了数据
            if(object==null){

                //尝试获取锁
                String lockKey= prefix+Arrays.toString(args)+ RedisConst.SKULOCK_SUFFIX;
                RLock lock = redissonClient.getLock(lockKey);
                //加锁
                boolean result = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //判断
                if(result){
                    try {
                        //查询数据库--执行方法体
                        object= joinPoint.proceed(args);
                        //判断
                        if(object==null){

                            //数据库中没有
                           object=aClass.newInstance();
                            redisTemplate.opsForValue().set(dataKey, JSONObject.toJSONString(object),RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                        }else{
                            redisTemplate.opsForValue().set(dataKey, JSONObject.toJSONString(object),RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                            return object;
                        }
                    } finally {
                        //释放锁
                        lock.unlock();

                    }


                }else{

                    Thread.sleep(100);

                    //自旋
                    return this.cacheAroundAdvice(joinPoint);

                }



            }else{

                //不为null
                return object;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }


        //查询数据-执行方法体
        return joinPoint.proceed(args);
    }

    /**
     * 从缓存中尝试获取数据
     *
     * 注意：json字符串
     *
     * @param dataKey
     * @return
     */
    private Object cacheHit(String dataKey,Class clazz) {
        //获取存储的数据
        String strJon = (String) redisTemplate.opsForValue().get(dataKey);
        //判断
        if(!StringUtils.isEmpty(strJon)){
            Object object = JSONObject.parseObject(strJon, clazz);
            return object;
        }


        return null;
    }



}
