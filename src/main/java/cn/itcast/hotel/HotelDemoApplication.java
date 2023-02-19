package cn.itcast.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication
@EnableConfigurationProperties
public class HotelDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(HotelDemoApplication.class, args);
    }

}
