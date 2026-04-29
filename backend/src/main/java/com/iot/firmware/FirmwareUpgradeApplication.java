package com.iot.firmware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FirmwareUpgradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(FirmwareUpgradeApplication.class, args);
    }
}
