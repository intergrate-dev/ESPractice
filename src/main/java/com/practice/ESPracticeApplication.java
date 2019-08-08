package com.practice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.naming.Context;

//使@RabbitListener生效
@EnableRabbit
@SpringBootApplication
public class ESPracticeApplication {
	private static Logger logger = LoggerFactory.getLogger(ESPracticeApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ESPracticeApplication.class, args);
	}
}
