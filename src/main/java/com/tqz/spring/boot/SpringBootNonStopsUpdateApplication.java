package com.tqz.spring.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;

/**
 * Spring Boot 不停机更新应用的启动类.
 *
 * @author <a href="https://github.com/tian-qingzhao">tianqingzhao</a>
 * @since 2024/7/25 9:25
 */
@SpringBootApplication
public class SpringBootNonStopsUpdateApplication {

    private static final Logger log = LoggerFactory.getLogger(SpringBootNonStopsUpdateApplication.class);

    public static void main(String[] args) {
        String[] newArgs = args.clone();
        int defaultPort = 8088;
        boolean needChangePort = false;
        if (isPortInUse(defaultPort)) {
            newArgs = new String[args.length + 1];
            System.arraycopy(args, 0, newArgs, 0, args.length);
            newArgs[newArgs.length - 1] = "--server.port=9090";
            needChangePort = true;
            log.info("端口需要暂时更换");
        }
        ConfigurableApplicationContext run = SpringApplication.run(SpringBootNonStopsUpdateApplication.class, newArgs);
        ConfigurableEnvironment environment = run.getEnvironment();

        log.info("服务启动成功，获取到端口号：{}", environment.getProperty("server.port"));

        if (needChangePort) {
            replaceServerPort(environment, defaultPort);

            String nowPort = environment.getProperty("server.port");
            log.info("服务已替换，获取到端口号：{}", nowPort);

            String command = String.format("lsof -i :%d | grep LISTEN | awk '{print $2}' | xargs kill -9", defaultPort);
            try {
                Runtime.getRuntime().exec(new String[]{"sh", "-c", command}).waitFor();
                log.info("杀掉 {} 端口成功", defaultPort);
                ServletWebServerFactory webServerFactory = getWebServerFactory(run);
                ((TomcatServletWebServerFactory) webServerFactory).setPort(defaultPort);
                log.info("准备更换端口为 {} ", defaultPort);
                // 休眠 100 毫秒，防止旧的应用没有被kill掉，启动新的tomcat容器时候报端口已占用
                Thread.sleep(100);
                WebServer webServer = webServerFactory.getWebServer(invokeSelfInitialize(((ServletWebServerApplicationContext) run)));
                webServer.start();
                log.info("已更换端口为 {} 成功，获取到配置文件的端口：{}", defaultPort, environment.getProperty("server.port"));

                ((ServletWebServerApplicationContext) run).getWebServer().stop();
            } catch (IOException | InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }

    private static void replaceServerPort(ConfigurableEnvironment environment, int defaultPort) {
        MutablePropertySources propertySources = environment.getPropertySources();

        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource instanceof EnumerablePropertySource) {
                String[] propertyNames = ((EnumerablePropertySource<?>) propertySource).getPropertyNames();
                for (String propertyName : propertyNames) {
                    if ("server.port".equals(propertyName)) {
                        propertySources.replace(propertySource.getName(), new PropertySource<Object>(propertySource.getName()) {
                            @Override
                            public Object getProperty(String name) {
                                if ("server.port".equals(name)) {
                                    return defaultPort;
                                }
                                return null;
                            }
                        });
                        break;
                    }
                }
            }
        }
    }

    private static ServletContextInitializer invokeSelfInitialize(ServletWebServerApplicationContext context) {
        try {
            Method method = ServletWebServerApplicationContext.class.getDeclaredMethod("getSelfInitializer");
            method.setAccessible(true);
            return (ServletContextInitializer) method.invoke(context);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

    }

    private static boolean isPortInUse(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    private static ServletWebServerFactory getWebServerFactory(ConfigurableApplicationContext context) {
        String[] beanNames = context.getBeanFactory().getBeanNamesForType(ServletWebServerFactory.class);

        return context.getBeanFactory().getBean(beanNames[0], ServletWebServerFactory.class);
    }
}