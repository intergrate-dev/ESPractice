package com.practice;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//使@RabbitListener生效
@EnableRabbit
@SpringBootApplication
public class ESPracticeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ESPracticeApplication.class, args);
	}
}
