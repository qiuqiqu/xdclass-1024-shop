package net.xdclass;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Version 1.0
 **/
@EnableFeignClients //开启Feign
@EnableDiscoveryClient //开启 nacos 服务注册和发现
@SpringBootApplication
@MapperScan("net.xdclass.mapper")
@EnableTransactionManagement //开启事物
public class CouponApplication {

    public static void main(String [] args){
        SpringApplication.run(CouponApplication.class,args);
    }

}
