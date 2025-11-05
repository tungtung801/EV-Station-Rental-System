package spring_boot.project_swp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProjectSwpApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProjectSwpApplication.class, args);
  }
}
