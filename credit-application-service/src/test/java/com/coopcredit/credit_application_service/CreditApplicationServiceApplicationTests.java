package com.coopcredit.credit_application_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CreditApplicationServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
