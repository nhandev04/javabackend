package com.bitas.ecommerce.utils.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JWT {

    private static final String HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final String HMAC_ALGO = "HmacSHA256";

    private static String base64UrlEncode(String data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] base64UrlDecode(String encoded) {
        return Base64.getUrlDecoder().decode(encoded);
    }

    // Sign payloadJson với secret key, tự động thêm exp nếu chưa có
    public static String sign(String payloadJson, String secretKey) throws Exception {
        // Thêm exp nếu chưa có
        if (!payloadJson.contains("\"exp\"")) {
            long exp = System.currentTimeMillis() / 1000L + 60 * 60; // 1 giờ
            if (payloadJson.endsWith("}")) {
                payloadJson = payloadJson.substring(0, payloadJson.length() - 1) + ",\"exp\":" + exp + "}";
            }
        }
        String headerB64 = base64UrlEncode(HEADER);
        String payloadB64 = base64UrlEncode(payloadJson);
        String dataToSign = headerB64 + "." + payloadB64;

        Mac mac = Mac.getInstance(HMAC_ALGO);
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
        mac.init(keySpec);

        byte[] signatureBytes = mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));
        String signatureB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);

        return dataToSign + "." + signatureB64;
    }

    // Verify JWT với secret key và kiểm tra hết hạn
    public static boolean verify(String jwt, String secretKey) throws Exception {
        String[] parts = jwt.split("\\.");
        if (parts.length != 3) return false;

        String headerB64 = parts[0];
        String payloadB64 = parts[1];
        String signatureB64 = parts[2];

        String dataToVerify = headerB64 + "." + payloadB64;

        Mac mac = Mac.getInstance(HMAC_ALGO);
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
        mac.init(keySpec);

        byte[] expectedSig = mac.doFinal(dataToVerify.getBytes(StandardCharsets.UTF_8));
        String expectedSigB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(expectedSig);

        if (!expectedSigB64.equals(signatureB64)) return false;

        // Kiểm tra hết hạn
        if (isExpired(jwt)) return false;

        return true;
    }

    // Lấy payload JSON từ JWT
    public static String getPayload(String jwt) {
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) return null;
        return new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
    }

    // Kiểm tra token hết hạn (payload phải có exp là epoch seconds)
    public static boolean isExpired(String jwt) {
        String payloadJson = getPayload(jwt);
        if (payloadJson == null) return true;
        try {
            // Đơn giản dùng split, không phụ thuộc thư viện JSON ngoài
            int expIdx = payloadJson.indexOf("\"exp\":");
            if (expIdx == -1) return false; // Không có exp thì coi như không hết hạn
            int start = expIdx + 6;
            while (start < payloadJson.length() && !Character.isDigit(payloadJson.charAt(start))) start++;
            int end = start;
            while (end < payloadJson.length() && Character.isDigit(payloadJson.charAt(end))) end++;
            if (start == end) return false;
            long exp = Long.parseLong(payloadJson.substring(start, end));
            long now = System.currentTimeMillis() / 1000L;
            return now > exp;
        } catch (Exception e) {
            return true;
        }
    }
}
