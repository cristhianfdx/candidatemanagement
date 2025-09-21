package com.cristhianfdx.candidatemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.cristhianfdx.candidatemanagement")
public class CandidateManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(CandidateManagementApplication.class, args);
	}

}
