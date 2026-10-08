package com.spendsense;

import com.spendsense.config.GeminiProperties;
import com.spendsense.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, GeminiProperties.class})
public class SpendSenseApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpendSenseApplication.class, args);
    }
}
