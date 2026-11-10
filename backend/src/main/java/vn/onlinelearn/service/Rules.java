package vn.onlinelearn.service;

import java.time.*;
import java.util.*;
import java.security.*;
import java.nio.charset.StandardCharsets;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.onlinelearn.repository.Data;
import vn.onlinelearn.entity.*;
import vn.onlinelearn.security.Actor;
import vn.onlinelearn.exception.ApiException;

@Service
public class Rules {
    @Autowired Data db;
    public static final Set<String> ROLES = Set.of("Customer","Admin","Author","Sale","Marketing","Staff");
    public static LocalDateTime now() { return LocalDateTime.now(Clock.systemUTC()); }
    public static String email(String email) { return email.strip().toLowerCase(Locale.ROOT); }
    public static String randomToken() { byte[] b = new byte[32]; new SecureRandom().nextBytes(b); return Base64.getUrlEncoder().withoutPadding().encodeToString(b); }
    public static String hash(String text) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8))); }
        catch(NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    public static String html(String raw) { return raw == null ? "" : Jsoup.clean(raw, Safelist.relaxed().addAttributes("a", "target", "rel")); }
    public static String url(String raw) {
        if(raw == null || raw.isBlank()) return null;
        if(raw.startsWith("/") && !raw.startsWith("//") && !raw.contains("\\")) return raw;
        try { var uri = java.net.URI.create(raw); if(Set.of("https","http").contains(uri.getScheme()) && uri.getHost()!=null && uri.getUserInfo()==null) return raw; } catch(Exception ignored) {}
        throw ApiException.bad("Đường dẫn phải là URL http/https hợp lệ.");
    }
    public static void password(String value, String confirm) {
        if(!value.equals(confirm)) throw ApiException.bad("Mật khẩu xác nhận không khớp.");
        if(value.length()<8 || value.getBytes(StandardCharsets.UTF_8).length>72) throw ApiException.bad("Mật khẩu cần từ 8 ký tự và tối đa 72 byte.");
    }
    public Actor optional() {
        var a=SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getPrincipal() instanceof Actor actor ? actor : null;
    }
    public Actor actor() { var a=optional(); if(a==null) throw new ApiException(401,"Vui lòng đăng nhập."); return a; }
    public Actor role(String... roles) { var a=actor(); if(!Arrays.asList(roles).contains(a.role())) throw ApiException.forbidden(); return a; }
    public Subject assigned(Long id) {
        var a=role("Admin","Author"); var s=db.subjects.lockById(id).orElseThrow(ApiException::missing);
        if(a.role().equals("Author") && !a.id().equals(s.ownerUserId)) throw ApiException.forbidden(); return s;
    }
    public Setting setting(Long id, String type) {
        var s=db.settings.findById(id).orElseThrow(ApiException::missing);
        if(!s.type.equals(type) || !s.status.equals("Active")) throw ApiException.bad("Danh mục không hợp lệ hoặc đã ngừng sử dụng."); return s;
    }
    public List<CourseAccess> accesses(Long userId, Long subjectId) {
        var time=now(); return db.access.findByUserId(userId).stream()
            .filter(x -> x.subjectId.equals(subjectId) && !time.isBefore(x.validFrom) && time.isBefore(x.validTo)).toList();
    }
    public void learning(Long subjectId, Long packageId) {
        var a=role("Customer"); var list=accesses(a.id(),subjectId);
        if(list.stream().noneMatch(x -> packageId==null || db.registrations.findById(x.registrationId).orElseThrow().packageId.equals(packageId)))
            throw new ApiException(403,"Bạn chưa có quyền học còn hiệu lực cho nội dung này.");
    }
    public static Map<String,Object> map(Object... values) {
        var m=new LinkedHashMap<String,Object>(); for(int i=0;i<values.length;i+=2) m.put(values[i].toString(),values[i+1]); return m;
    }
    public static <T> Map<String,Object> page(List<T> items,int page,int size) {
        if(page<0 || size<1 || size>100) throw ApiException.bad("Phân trang không hợp lệ.");
        int from=(int)Math.min((long)page*size,items.size()); int to=Math.min(from+size,items.size());
        return map("content",items.subList(from,to),"totalElements",items.size(),"totalPages",(items.size()+size-1)/size,"number",page,"size",size);
    }
}
