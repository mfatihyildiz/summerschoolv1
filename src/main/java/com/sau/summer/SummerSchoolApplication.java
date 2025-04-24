package com.sau.summer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.sau.summer.repository")
public class SummerSchoolApplication {

    public static void main(String[] args) {
        SpringApplication.run(SummerSchoolApplication.class, args);
    }

}
