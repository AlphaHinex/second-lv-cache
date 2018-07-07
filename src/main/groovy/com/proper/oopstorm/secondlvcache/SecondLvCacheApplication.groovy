package com.proper.oopstorm.secondlvcache

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ImportResource

@SpringBootApplication
@ImportResource("classpath*:spring/applicationContext.xml")
class SecondLvCacheApplication {

	static void main(String[] args) {
		SpringApplication.run SecondLvCacheApplication, args
	}
}
