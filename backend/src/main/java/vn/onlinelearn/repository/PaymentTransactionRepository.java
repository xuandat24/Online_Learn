package vn.onlinelearn.repository;

import vn.onlinelearn.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long>, JpaSpecificationExecutor<PaymentTransaction> {
    @Query("select p.registrationId from PaymentTransaction p where p.orderCode = :code and p.provider = 'SEPAY'")
    Optional<Long> sepayRegistrationId(@Param("code") String code);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PaymentTransaction> findByOrderCode(String orderCode);
    List<PaymentTransaction> findByRegistrationIdOrderByCreatedAtDesc(Long registrationId);
    boolean existsByProviderTxnRef(String providerTxnRef);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from PaymentTransaction e where e.paymentId = :id")
    Optional<PaymentTransaction> lockById(@Param("id") Long id);
}
