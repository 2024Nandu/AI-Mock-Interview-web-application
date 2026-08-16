package com.example.demo;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class DemoApplication {

	static {
		loadEnv();
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	public static void loadEnv() {
		File envInCurrent = new File(".env");
		File envInBackend = new File("backend/.env");

		DotenvBuilder builder = Dotenv.configure().ignoreIfMissing();

		if (envInCurrent.exists()) {
			builder.directory("./");
		} else if (envInBackend.exists()) {
			builder.directory("./backend");
		}

		Dotenv dotenv = builder.load();
		dotenv.entries().forEach(entry -> {
			if (System.getProperty(entry.getKey()) == null) {
				System.setProperty(entry.getKey(), entry.getValue());
			}
		});
	}
}
