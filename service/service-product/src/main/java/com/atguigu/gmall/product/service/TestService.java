package com.atguigu.gmall.product.service;

public interface TestService {
    /**
     * 锁的演示
     */
    void testLock();

    /**
     * 读取数据
     * @return
     */
    String read();

    /**
     * 写入数据
     * @return
     */
    String write();
}
