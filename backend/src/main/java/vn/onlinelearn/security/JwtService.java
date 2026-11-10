package vn.onlinelearn.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import vn.onlinelearn.entity.User;

@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final long minutes;
    public JwtService(@Value("${app.jwt-secret}") String secret, @Value("${app.token-minutes}") long minutes) {
        if(secret.getBytes(StandardCharsets.UTF_8).length < 32) throw new IllegalArgumentException("JWT_SECRET must contain at least 32 bytes");
        var key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        var dec = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        dec.setJwtValidator(JwtValidators.createDefaultWithIssuer("onlinelearn"));
        decoder = dec;
        this.minutes = minutes;
    }
    public String issue(User user) {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("onlinelearn").subject(user.userId.toString()).issuedAt(now)
            .expiresAt(now.plusSeconds(minutes * 60)).claim("version", user.tokenVersion).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
    public Jwt decode(String token) { return decoder.decode(token); }
}
