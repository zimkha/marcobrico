package com.mmd.marcobrico;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "API marcobrico ",
				version = "v1",
				description = "Une description  marcobrico",
				contact = @Contact(
						name = "Khazim",
						email = "zimkhandiaye@gmail.com",
						url = "https://www.linkedin.com/in/khazim-ndiaye-b341a9168/"

				),
				license = @License(
						name = "Apache 2.0"
				)
		)
)
public class MarcobricoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarcobricoApplication.class, args);
	}

}
