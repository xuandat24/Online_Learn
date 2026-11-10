package vn.onlinelearn.service;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.math.BigDecimal;
import com.fasterxml.jackson.databind.*;
import vn.onlinelearn.repository.*;
import vn.onlinelearn.entity.*;
import vn.onlinelearn.integration.SePayGatewayClient;
import vn.onlinelearn.exception.ApiException;
class PaymentServiceTest {
    PaymentService s; Data db; PaymentTransaction p; Registration r; ObjectMapper json=new ObjectMapper();
    @BeforeEach void setup(){s=new PaymentService();db=new Data();db.payments=mock(PaymentTransactionRepository.class);db.registrations=mock(RegistrationRepository.class);db.access=mock(CourseAccessRepository.class);s.db=db;s.gateway=mock(SePayGatewayClient.class);s.maxAge=86400;r=new Registration();r.registrationId=1L;r.totalCost=new BigDecimal("100000");p=new PaymentTransaction();p.paymentId=1L;p.registrationId=1L;p.orderCode="OLtest";p.provider="SEPAY";p.amount=r.totalCost;when(db.payments.findByRegistrationIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(p));when(db.payments.sepayRegistrationId("OLtest")).thenReturn(Optional.of(1L));when(db.registrations.lockById(1L)).thenReturn(Optional.of(r));when(db.payments.findByOrderCode("OLtest")).thenReturn(Optional.of(p));}
    JsonNode order(String status){return json.valueToTree(Rules.map("order_invoice_number","OLtest","order_status",status,"order_amount","100000.00","order_currency","VND","transactions",List.of()));}
    @Test void pendingCheckoutIsReused(){when(s.gateway.checkout(p)).thenReturn(Map.of("method","POST"));assertNotNull(s.create(r));verify(db.payments,never()).save(any());}
    @Test void legacyPendingIsNotReplaced(){p.provider="VNPAY";assertThrows(ApiException.class,()->s.create(r));verify(db.payments,never()).save(any());}
    @Test void apiPendingDoesNotGrant(){when(s.gateway.query(p)).thenReturn(order("AUTHENTICATION_NOT_NEEDED"));s.recheck(r);assertEquals("Pending",p.status);verifyNoInteractions(db.access);}
    @Test void apiCancellationDoesNotGrant(){when(s.gateway.query(p)).thenReturn(order("CANCELLED"));s.recheck(r);assertEquals("Cancelled",p.status);assertEquals("Submitted",r.status);verifyNoInteractions(db.access);}
    @Test void apiFailureRemainsPending(){when(s.gateway.query(p)).thenThrow(new ApiException(503,"unavailable"));assertThrows(ApiException.class,()->s.recheck(r));assertEquals("Pending",p.status);verifyNoInteractions(db.access);}
    @Test void browserLikePayloadCannotConfirm(){when(s.gateway.authenticate("test")).thenReturn(true);var b=json.valueToTree(Rules.map("timestamp",java.time.Instant.now().getEpochSecond(),"notification_type","ORDER_PAID","order",order("CAPTURED"),"transaction",Map.of("transaction_status","APPROVED")));assertThrows(ApiException.class,()->s.ipn("test",b));assertEquals("Submitted",r.status);verifyNoInteractions(db.access);}
}
