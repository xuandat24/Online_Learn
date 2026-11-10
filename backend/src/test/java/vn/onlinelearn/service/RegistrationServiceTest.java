package vn.onlinelearn.service;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.math.BigDecimal;
import vn.onlinelearn.repository.*;
import vn.onlinelearn.entity.*;
import vn.onlinelearn.integration.*;
class RegistrationServiceTest {
    RegistrationService service;Data db;PaymentTransaction p;Registration r;
    @BeforeEach void setup(){service=new RegistrationService();db=new Data();db.payments=mock(PaymentTransactionRepository.class);db.registrations=mock(RegistrationRepository.class);db.access=mock(CourseAccessRepository.class);db.users=mock(UserRepository.class);db.packages=mock(PricePackageRepository.class);service.db=db;service.gateway=mock(VnpayClient.class);service.auth=mock(AuthService.class);service.mail=mock(MailService.class);
        p=new PaymentTransaction();p.registrationId=1L;p.amount=new BigDecimal("100000");r=new Registration();r.registrationId=1L;r.packageId=2L;r.email="learner@example.test";r.totalCost=p.amount;r.durationMonths=3;
        when(db.payments.findByOrderCode("test-order")).thenReturn(Optional.of(p));when(db.registrations.lockById(1L)).thenReturn(Optional.of(r));}
    Map<String,String> values(String code,String status){return Map.of("vnp_TxnRef","test-order","vnp_Amount","10000000","vnp_ResponseCode",code,"vnp_TransactionStatus",status,"vnp_TransactionNo","123456");}
    @Test void invalidSignatureDoesNotTouchDatabase(){assertEquals("97",service.callback(values("00","00")).get("RspCode"));verifyNoInteractions(db.payments,db.access);}
    @Test void wrongAmountDoesNotGrantAccess(){when(service.gateway.verify(anyMap())).thenReturn(true);var v=new HashMap<>(values("00","00"));v.put("vnp_Amount","1");assertEquals("04",service.callback(v).get("RspCode"));assertEquals("Submitted",r.status);verifyNoInteractions(db.access);}
    @Test void pendingDoesNotGrantAccess(){when(service.gateway.verify(anyMap())).thenReturn(true);service.callback(values("00","01"));assertEquals("Pending",p.status);verifyNoInteractions(db.access);}
    @Test void failedDoesNotGrantAccess(){when(service.gateway.verify(anyMap())).thenReturn(true);service.callback(values("09","02"));assertEquals("Failed",p.status);assertEquals("Submitted",r.status);verifyNoInteractions(db.access);}
    @Test void cancelledDoesNotGrantAccess(){when(service.gateway.verify(anyMap())).thenReturn(true);service.callback(values("24","02"));assertEquals("Cancelled",p.status);verifyNoInteractions(db.access);}
    @Test void verifiedPaymentGrantsOnceAndUsesPurchasedDuration(){when(service.gateway.verify(anyMap())).thenReturn(true);var u=new User();u.userId=7L;u.roleCode="Customer";u.email=r.email;when(db.users.findByEmail(r.email)).thenReturn(Optional.of(u));var pkg=new PricePackage();pkg.subjectId=9L;pkg.durationMonths=99;when(db.packages.findById(2L)).thenReturn(Optional.of(pkg));assertEquals("00",service.callback(values("00","00")).get("RspCode"));assertEquals("Paid",r.status);assertEquals(r.validFrom.plusMonths(3),r.validTo);assertEquals("02",service.callback(values("00","00")).get("RspCode"));verify(db.access,times(1)).save(any());}
    @Test void duplicateProviderReferenceDoesNotGrant(){when(service.gateway.verify(anyMap())).thenReturn(true);when(db.payments.existsByProviderTxnRef("123456")).thenReturn(true);assertEquals("02",service.callback(values("00","00")).get("RspCode"));verifyNoInteractions(db.access);}
}
