package net.xdclass;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @Description
 * @Version 1.0
 **/
@SpringBootApplication
@MapperScan("net.xdclass.mapper")
public class UserApplication {

    public static void main(String [] args){
        SpringApplication.run(UserApplication.class,args);
    }

}
