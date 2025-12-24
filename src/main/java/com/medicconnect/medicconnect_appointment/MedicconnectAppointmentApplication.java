package com.medicconnect.medicconnect_appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MedicconnectAppointmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedicconnectAppointmentApplication.class, args);
	}

}
