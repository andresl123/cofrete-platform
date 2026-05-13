package com.cofrete.dataimporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DataImporterWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataImporterWorkerApplication.class, args);
    }
}
