package net.xdclass.config;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器
 **/

@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new LoginInterceptor())
                //拦截的路径
                .addPathPatterns("/api/order/*/**")

                //排查不拦截的路径
                .excludePathPatterns(
                        "/api/callback/*/**",
                        "/api/order/*/query_state",
                        "/api/order/*/test_pay",
                        "/api/coupon_record/v1/lock_records",
                        "/api/coupon_record/v1/*/**",
                        "/api/cart/v1/confirm_order_cart_items",
                        "/api/product/v1/lock_products",
                        "/api/address/v1/*/**");

    }
}
