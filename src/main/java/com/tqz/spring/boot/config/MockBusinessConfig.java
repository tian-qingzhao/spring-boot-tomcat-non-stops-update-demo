package com.tqz.spring.boot.config;

import com.tqz.spring.boot.SpringBootNonStopsUpdateApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * 模拟业务逻辑处理.
 *
 * @author <a href="https://github.com/tian-qingzhao">tianqingzhao</a>
 * @since 2024/7/25 10:43
 */
@Component
public class MockBusinessConfig implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(SpringBootNonStopsUpdateApplication.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("开始执行 StarterConfig 的初始化方法");

        Thread.sleep(10000);

        log.info("StarterConfig 的初始化方法 执行完成");
    }
}
