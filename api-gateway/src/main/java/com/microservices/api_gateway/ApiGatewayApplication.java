package com.microservices.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
		basePackages = "com.microservices",
		excludeFilters = {
				@ComponentScan.Filter(
						type = FilterType.REGEX,
						pattern = "com\\.microservices\\.common\\.security\\..*"
				),
				@ComponentScan.Filter(
						type = FilterType.REGEX,
						pattern = "com\\.microservices\\.common\\.config\\.SecurityConfig"
				),
				@ComponentScan.Filter(
						type = FilterType.REGEX,
						pattern = "com\\.microservices\\.common\\.reactive\\..*"
				)
		}
)
public class ApiGatewayApplication {
	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}
}