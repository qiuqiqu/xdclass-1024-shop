package net.xdclass.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private String redisPort;


    /**
     * 配置分布式锁
     * @return
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        //单机模式
        //config.useSingleServer().setPassword("123456").setAddress("redis://8.129.113.233:3308");
        config.useSingleServer().setAddress("redis://"+redisHost+":"+redisPort);

        //集群模式
        //config.useClusterServers()
        //.setScanInterval(2000)
        //.addNodeAddress("redis://10.0.29.30:6379", "redis://10.0.29.95:6379")
        // .addNodeAddress("redis://127.0.0.1:6379");

        RedissonClient redisson = Redisson.create(config);

        return redisson;
    }
}
