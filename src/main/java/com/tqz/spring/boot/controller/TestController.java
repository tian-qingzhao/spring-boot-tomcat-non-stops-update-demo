package com.tqz.spring.boot.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试控制器.
 *
 * @author <a href="https://github.com/tian-qingzhao">tianqingzhao</a>
 * @since 2024/7/25 9:38
 */
@RestController
public class TestController {

    private AtomicInteger value = new AtomicInteger(0);

    @RequestMapping("test")
    public String test(@RequestParam(defaultValue = "abc") String str) {
        return "v2";
    }

    @RequestMapping("test2")
    public String test2(@RequestParam(defaultValue = "abc") String str) {
        value.incrementAndGet();
        System.out.println("接收到的参数：" + str + " ，成员变量value：" + value.get());
        return str;
    }
}
