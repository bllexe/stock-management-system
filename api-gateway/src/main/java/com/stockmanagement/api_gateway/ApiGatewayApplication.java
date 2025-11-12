package com.stockmanagement.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication()
@EnableDiscoveryClient
public class ApiGatewayApplication {

	public static void main(String[] args) {

		SpringApplication.run(ApiGatewayApplication.class, args);

	}


	//  @Bean
    // CommandLineRunner runner(ApplicationContext ctx) {
    //     return args -> {
    //         System.out.println("🔍 Loaded GatewayFilterFactory beans:");
    //         Arrays.stream(ctx.getBeanNamesForType(org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory.class))
    //                 .forEach(System.out::println);
    //     };
    // }

}

