package com.example.demo.config;

import com.example.demo.entity.InterviewRole;
import com.example.demo.repository.InterviewRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);
    private final InterviewRoleRepository roleRepository;

    public DatabaseSeeder(InterviewRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            logger.info("No interview roles found in database. Seeding default roles...");
            
            List<InterviewRole> roles = new ArrayList<>();
            
            roles.add(new InterviewRole(
                    null,
                    "fresher",
                    "Fresher CS Track",
                    "Designed for graduating students and entry-level positions. Focuses on core computer science concepts, object-oriented design, fundamental algorithms, and basic logic.",
                    "The interview must focus on core computer science topics: Data Structures (Arrays, Lists, Stacks, Queues, Trees), Algorithms (Sorting, Searching), OOP concepts (Inheritance, Polymorphism, Encapsulation, Abstraction), and SQL database queries. Ask simple to medium questions suited for a fresh graduate."
            ));
            
            roles.add(new InterviewRole(
                    null,
                    "frontend",
                    "Frontend Developer",
                    "Tailored for web developers focused on the user interface. Covers HTML, CSS, JavaScript, React concepts, state management, UI performance, and browser APIs.",
                    "The interview must focus on frontend concepts: JavaScript basics (closures, event loop, promises, ES6+), React core concepts (hooks lifecycle, context, state management, render optimization), DOM manipulation, CSS layouts (Flexbox, Grid), and responsive web development."
            ));
            
            roles.add(new InterviewRole(
                    null,
                    "backend",
                    "Backend Developer",
                    "Designed for engineers focusing on logic and server operations. Covers Java, Spring Boot, JPA/Hibernate, databases, caching, and REST API design.",
                    "The interview must focus on backend concepts: REST API design standards, Java core and concurrency, Spring Boot annotations, Dependency Injection, Spring Security, JPA/Hibernate entity mappings, SQL queries, transaction management, caching (Redis), and system design basics."
            ));
            
            roles.add(new InterviewRole(
                    null,
                    "fullstack",
                    "Full-Stack Developer",
                    "Suitable for versatile engineers managing end-to-end architectures. Integrates UI concepts with database scaling and backend REST services.",
                    "The interview must focus on end-to-end concepts: Frontend integration with REST APIs, state synchronization, database optimization, caching layers, server deployment, load balancing, CORS security, JWT session management, and overall web architectural planning."
            ));
            
            roles.add(new InterviewRole(
                    null,
                    "devops",
                    "DevOps Engineer",
                    "Focused on continuous delivery, systems automation, cloud infrastructure, and deployment stability.",
                    "The interview must focus on DevOps practices: CI/CD configuration (GitHub Actions, Jenkins), containerization (Docker), container management (Kubernetes), Infrastructure as Code (Terraform), Cloud Providers (AWS/GCP), networking layers, and log tracking/monitoring (Prometheus, ELK)."
            ));
            
            roles.add(new InterviewRole(
                    null,
                    "data_analyst",
                    "Data Analyst",
                    "Designed for specialists in data extraction and insights. Focuses on SQL, Python libraries, statistics, and business metrics.",
                    "The interview must focus on analytical processes: Advanced SQL (joins, window functions, aggregates), data preparation, Python pandas/numpy basics, mathematical metrics (mean, median, standard deviation), and data dashboard visualization configurations."
            ));
            
            roles.add(new InterviewRole(
                    null,
                    "android",
                    "Android Developer",
                    "Tailored for mobile application engineers. Focuses on Kotlin, Android SDK, and architectural design patterns.",
                    "The interview must focus on mobile parameters: Kotlin programming, Android app lifecycle (Activities, Fragments), Jetpack Compose, MVVM architectural models, API integration (Retrofit), database storage (Room), and memory performance guidelines."
            ));
            
            roles.add(new InterviewRole(
                    null,
                    "professional",
                    "Experienced SDE (Professional)",
                    "Focuses on high-level architecture, systems scalability, team leadership scenarios, and advanced engineering patterns.",
                    "The interview must focus on professional-grade capabilities: High-level system design, microservices architecture, message brokers (Kafka/RabbitMQ), distributed caching, database indexing and clustering, design patterns, clean code structures, and engineering leadership behaviors."
            ));
            
            roleRepository.saveAll(roles);
            logger.info("Successfully seeded {} default interview roles.", roles.size());
        } else {
            logger.info("Interview roles already seeded in database (count: {}). Skipping seeder.", roleRepository.count());
        }
    }
}
