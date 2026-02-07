package com.example.backend.dict.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * 第三方接口签名工具：生成 Timestamp、specialUrlEncode、待签名串、HmacSHA1+Base64 签名。
 */
public final class ThirdPartySignatureUtil {

    private static final String CHARSET = "UTF-8";
    private static final String ALGORITHM_HMAC_SHA1 = "HmacSHA1";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_ZONE = "Asia/Shanghai";

    private ThirdPartySignatureUtil() {
    }

    /**
     * 生成当前时间戳字符串，格式 yyyy-MM-dd HH:mm:ss，时区 Asia/Shanghai
     */
    public static String generateTimestamp() {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        return df.format(new java.util.Date());
    }

    /**
     * POP 特殊 URL 编码：URLEncode 后 + -> %20, * -> %2A, %7E -> ~
     */
    public static String specialUrlEncode(String value) throws UnsupportedEncodingException {
        return java.net.URLEncoder.encode(value, CHARSET)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    /**
     * 构造待签名串：HTTPMethod & specialUrlEncode(url) & specialUrlEncode(sortedQueryString)
     * queryParams 中不应包含 Signature；会自动加入 appKey、timestamp 后按 key 排序拼接。
     */
    public static String buildStringToSign(String httpMethod, String path, Map<String, Object> queryParams,
                                          String appKey, String timestamp) throws UnsupportedEncodingException {
        Map<String, Object> parasMap = new TreeMap<>(queryParams);
        parasMap.put("appKey", appKey);
        parasMap.put("timestamp", timestamp);

        StringBuilder sortedQuery = new StringBuilder();
        for (Map.Entry<String, Object> e : parasMap.entrySet()) {
            if (sortedQuery.length() > 0) {
                sortedQuery.append("&");
            }
            sortedQuery.append(specialUrlEncode(e.getKey()))
                    .append("=")
                    .append(specialUrlEncode(String.valueOf(e.getValue())));
        }
        String sortedQueryString = sortedQuery.toString();
        return httpMethod.toUpperCase() + "&" + specialUrlEncode(path) + "&" + specialUrlEncode(sortedQueryString);
    }

    /**
     * 使用 HmacSHA1 + Base64(UTF-8)，密钥为 appSecret + "&"
     */
    public static String sign(String appSecret, String stringToSign) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        String signingKey = appSecret + "&";
        SecretKeySpec keySpec = new SecretKeySpec(signingKey.getBytes(CHARSET), ALGORITHM_HMAC_SHA1);
        Mac mac = Mac.getInstance(ALGORITHM_HMAC_SHA1);
        mac.init(keySpec);
        byte[] signData = mac.doFinal(stringToSign.getBytes(CHARSET));
        return Base64.getEncoder().encodeToString(signData);
    }

    /**
     * 主入口：根据方法、路径、查询参数、appKey、appSecret、timestamp 生成 Signature
     */
    public static String generateSignature(String httpMethod, String path, Map<String, Object> queryParams,
                                           String appKey, String appSecret, String timestamp) {
        try {
            String stringToSign = buildStringToSign(httpMethod, path, queryParams, appKey, timestamp);
            return sign(appSecret, stringToSign);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("生成签名失败", e);
        }
    }
}
