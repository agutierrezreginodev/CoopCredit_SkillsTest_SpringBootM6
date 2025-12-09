package com.coopcredit.credit_application_service;

import org.springframework.boot.SpringApplication;

public class TestCreditApplicationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(CreditApplicationServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
