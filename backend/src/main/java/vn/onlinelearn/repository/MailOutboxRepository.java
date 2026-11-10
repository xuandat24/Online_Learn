package vn.onlinelearn.repository;
import vn.onlinelearn.entity.MailOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MailOutboxRepository extends JpaRepository<MailOutbox,Long> {
    List<MailOutbox> findTop10ByStatusAndAttemptsLessThanOrderByCreatedAtAsc(String status,int attempts);
}
