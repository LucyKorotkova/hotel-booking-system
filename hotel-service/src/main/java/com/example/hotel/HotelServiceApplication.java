package com.example.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.example.hotel", "com.example.common"})
public class HotelServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HotelServiceApplication.class, args);
    }
}
