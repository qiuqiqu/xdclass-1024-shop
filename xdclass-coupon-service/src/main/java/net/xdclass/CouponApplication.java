package net.xdclass;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Version 1.0
 **/
@SpringBootApplication
@MapperScan("net.xdclass.mapper")
public class CouponApplication {

    public static void main(String [] args){
        SpringApplication.run(CouponApplication.class,args);
    }

}
