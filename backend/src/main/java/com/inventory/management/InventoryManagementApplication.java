package com.inventory.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
public class InventoryManagementApplication {

    public static void main(String[] args) {
        // Persist application timestamps consistently in UTC; clients format them locally.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(InventoryManagementApplication.class, args);
    }
}
