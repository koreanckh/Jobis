package co.jobis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
public class JobisApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobisApplication.class, args);
    }

}
