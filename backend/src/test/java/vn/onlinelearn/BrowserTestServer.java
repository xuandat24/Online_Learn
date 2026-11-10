package vn.onlinelearn;

/** Explicit browser-test harness only; never packaged in the production JAR. */
public class BrowserTestServer {
    public static void main(String[] args){
        org.springframework.boot.SpringApplication.run(OnlineLearnApplication.class,
            "--spring.profiles.active=test",
            "--server.port=18080",
            "--spring.datasource.url=jdbc:h2:mem:browser_test;MODE=MSSQLServer;NON_KEYWORDS=VALUE;DB_CLOSE_DELAY=-1",
            "--spring.datasource.driver-class-name=org.h2.Driver","--spring.datasource.username=sa","--spring.datasource.password=",
            "--spring.flyway.enabled=false","--spring.jpa.hibernate.ddl-auto=create-drop","--spring.jpa.properties.hibernate.hbm2ddl.halt_on_error=true",
            "--app.jwt-secret=browser-test-only-key-not-for-production-32-bytes",
            "--app.demo-seed=true","--app.demo-password=BrowserTestOnly!2026","--app.mail-mode=disabled");
    }
}
