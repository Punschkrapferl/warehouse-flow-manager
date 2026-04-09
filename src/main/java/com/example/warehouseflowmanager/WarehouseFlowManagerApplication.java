package com.example.warehouseflowmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;

/*
 * Main entry point of the Spring Boot application.
 *
 * @SpringBootApplication starts the application and enables Spring Boot's
 * automatic configuration and component scanning.
 *
 * The exclude block is temporary.
 * It tells Spring Boot not to auto-configure:
 * 1. the database connection
 * 2. Hibernate / JPA
 *
 * We are doing this only for the beginner bootstrap phase so the project
 * can run before the real database is configured properly.
 */
@SpringBootApplication(
		exclude = {
				DataSourceAutoConfiguration.class,
				HibernateJpaAutoConfiguration.class
		}
)
public class WarehouseFlowManagerApplication {

	public static void main(String[] args) {
		// Starts the Spring Boot application and the embedded server.
		SpringApplication.run(WarehouseFlowManagerApplication.class, args);
	}
}