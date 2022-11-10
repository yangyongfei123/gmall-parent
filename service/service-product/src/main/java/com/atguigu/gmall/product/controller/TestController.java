package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/product/test")
public class TestController {

    @Autowired
    private TestService testService;

    /**
     * 测试本地锁
     * @return
     */
    @GetMapping("/testLock")
    public Result testLock(){

        testService.testLock();
        return Result.ok();
    }

    /**
     * 读写锁-读取数据
     * @return
     */
    @GetMapping("/read")
    public String read(){


        return testService.read();
    }

    /**
     * 读写锁-写入数据
     * @return
     */
    @GetMapping("/write")
    public String write(){


        return testService.write();
    }


}
