package com.universityprinting.printing_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.mongodb.uri=mongodb://localhost:27017/test_db")
class PrintingBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
