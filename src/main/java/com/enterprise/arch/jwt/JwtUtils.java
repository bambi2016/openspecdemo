package com.enterprise.arch.jwt;

import com.enterprise.arch.auth.LoginUser;
import com.enterprise.common.error.CommonErrorCode;
import com.enterprise.common.exception.BizException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final JwtProperties properties;

    public JwtUtils(JwtProperties properties) {
        this.properties = properties;
    }

    public String generateToken(Long userId, String username) {
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("userId", userId);
            payload.put("username", username);
            payload.put("exp", Instant.now().getEpochSecond() + properties.getExpirationSeconds());

            String encodedHeader = encodeJson(header);
            String encodedPayload = encodeJson(payload);
            String unsigned = encodedHeader + "." + encodedPayload;
            return unsigned + "." + sign(unsigned);
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "Token 生成失败");
        }
    }

    public LoginUser parseToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new BizException(CommonErrorCode.TOKEN_INVALID);
            }
            String unsigned = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(sign(unsigned).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new BizException(CommonErrorCode.TOKEN_INVALID);
            }
            Map<String, Object> payload = OBJECT_MAPPER.readValue(URL_DECODER.decode(parts[1]), new TypeReference<>() {
            });
            Number exp = (Number) payload.get("exp");
            if (exp == null || exp.longValue() < Instant.now().getEpochSecond()) {
                throw new BizException(CommonErrorCode.TOKEN_INVALID);
            }
            Number userId = (Number) payload.get("userId");
            String username = (String) payload.get("username");
            if (userId == null || username == null) {
                throw new BizException(CommonErrorCode.TOKEN_INVALID);
            }
            return new LoginUser(userId.longValue(), username);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(CommonErrorCode.TOKEN_INVALID);
        }
    }

    public long getExpirationSeconds() {
        return properties.getExpirationSeconds();
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        return URL_ENCODER.encodeToString(OBJECT_MAPPER.writeValueAsBytes(value));
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(properties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }
}
