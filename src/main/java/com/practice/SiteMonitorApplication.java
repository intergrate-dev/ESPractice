package com.practice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//使@RabbitListener生效
@EnableRabbit
@SpringBootApplication
public class SiteMonitorApplication {
	private static Logger logger = LoggerFactory.getLogger(SiteMonitorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SiteMonitorApplication.class, args);
	}
}
