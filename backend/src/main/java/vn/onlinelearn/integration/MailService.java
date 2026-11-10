package vn.onlinelearn.integration;

import java.nio.file.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.onlinelearn.entity.MailOutbox;
import vn.onlinelearn.repository.MailOutboxRepository;
import vn.onlinelearn.service.Rules;

@Service
public class MailService {
    @Autowired MailOutboxRepository outbox;
    @Autowired JavaMailSender sender;
    @Value("${app.mail-mode}") String mode;
    @Value("${app.mail-directory}") String directory;
    @Value("${spring.mail.username}") String from;
    @Value("${spring.mail.password}") String password;
    public void enqueue(String recipient,String subject,String body) {
        var m=new MailOutbox(); m.recipient=recipient; m.subject=subject; m.body=body; outbox.save(m);
    }
    @Scheduled(fixedDelay=15000, initialDelay=10000)
    @Transactional
    public void deliver() {
        if(mode.equals("smtp") && (from.isBlank() || password.isBlank())) return;
        for(var row:outbox.findTop10ByStatusAndAttemptsLessThanOrderByCreatedAtAsc("Pending",5)) {
            try {
                if(mode.equals("file")) {
                    var folder=Path.of(directory); Files.createDirectories(folder);
                    Files.writeString(folder.resolve(row.mailId+".eml"),"X-OnlineLearn-Mode: LOCAL TEST ONLY - NOT SENT\nTo: "+row.recipient+"\nSubject: "+row.subject+"\n\n"+row.body);
                    row.status="LocalFile";
                } else if(mode.equals("smtp")) {
                    var m=new SimpleMailMessage(); m.setFrom(from); m.setTo(row.recipient); m.setSubject(row.subject); m.setText(row.body); sender.send(m); row.status="Sent";
                } else return;
                row.sentAt=Rules.now(); row.body="[Delivered; token link removed from outbox]";
            } catch(Exception e) { row.attempts++; }
            outbox.save(row);
        }
    }
}
