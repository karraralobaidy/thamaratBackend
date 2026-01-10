package com.earn.earnmoney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
public class EarnmoneyApplication {

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Baghdad"));
	}

	public static void main(String[] args) {
		SpringApplication.run(EarnmoneyApplication.class, args);
	}

}
