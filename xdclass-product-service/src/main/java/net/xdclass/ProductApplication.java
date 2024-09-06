package net.xdclass;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Version 1.0
 **/
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("net.xdclass.mapper")
public class ProductApplication {

    public static void main(String [] args){

        SpringApplication.run(ProductApplication.class,args);
    }

}
