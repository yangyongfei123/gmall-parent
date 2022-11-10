package com.atguigu.gmall.product.controller;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompletableFutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

//        //1.创建异步对象--没有返回值
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//
//
//            System.out.println(Thread.currentThread().getName()+"1111111");
//        });
//        //2.创建异步对象--有返回值
//        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
//
//            System.out.println(Thread.currentThread().getName()+"3333");
//            return 100;
//        });
//
//        System.out.println(completableFuture.get());
//
//        future.join();
//        System.out.println(Thread.currentThread().getName()+"22222");

//        //创建一个又返回值的对象
//        CompletableFuture<Object> future = CompletableFuture.supplyAsync(new Supplier<Object>() {
//            @Override
//            public Object get() {
//
//                int i=1/0;
//
//                return 1024;
//            }
//        }).whenCompleteAsync(new BiConsumer<Object, Throwable>() {
//            @Override
//            public void accept(Object o, Throwable throwable) {
//
//                System.out.println("返回的结果：" + o);
//                System.out.println("返回的异常：" + throwable);
//
//            }
//        }).exceptionally(new Function<Throwable, Object>() {
//            @Override
//            public Object apply(Throwable throwable) {
//
//                System.out.println("异常信息"+throwable);
//                System.out.println("异常信息"+throwable.getMessage());
//
//                return 500;
//            }
//        });
//
//        System.out.println(future.get());
//

        //创建A异步对象
        CompletableFuture<Object> futureA = CompletableFuture.supplyAsync(new Supplier<Object>() {
            @Override
            public Object get() {

                System.out.println("A异步对象执行了。。。。。");
                return 400;
            }
        });

        //创建B
        CompletableFuture<Void> futureB = futureA.thenAccept(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("B异步对象接收的数据\t" + o);
            }
        });

        //创建C
        CompletableFuture<Void> futureC = futureA.thenAccept(new Consumer<Object>() {
            @Override
            public void accept(Object o) {

                System.out.println("C异步对象接收的数据\t" + o);
            }
        });


//        futureB.join();
    }
}
