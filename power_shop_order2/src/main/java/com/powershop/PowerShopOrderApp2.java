package com.powershop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.powershop.mapper")
public class PowerShopOrderApp2 {
    public static void main(String[] args) {
        SpringApplication.run(PowerShopOrderApp2.class,args);
    }
}