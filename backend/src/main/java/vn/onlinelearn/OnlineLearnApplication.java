package vn.onlinelearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OnlineLearnApplication {
    public static void main(String[] args) { SpringApplication.run(OnlineLearnApplication.class, args); }
}
