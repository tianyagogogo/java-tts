package com.lzt.tts.msTts.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.io.IOUtils;


public class MsTtsUtils2 {
    private static final HttpURLConnection client = null; // 使用时需要初始化
    private static List<Object> voiceListCache = null;
    private static final long cacheDuration = 3600000; // 1小时，以毫秒为单位

    private static final String endpointURL = "https://dev.microsofttranslator.com/apps/endpoint?api-version=1.0";
    private static final String voicesListURL = "https://eastus.api.speech.microsoft.com/cognitiveservices/voices/list";
    private static final String userAgent = "okhttp/4.5.0";
    private static final String clientVersion = "4.0.530a 5fe1dc6c";
    private static final String userId = "0f04d16a175c411e";
    private static final String homeGeographicRegion = "zh-Hans-CN";
    private static final String clientTraceId = "aab069b9-70a7-4844-a734-96cd78d94be9";
    private static final String voiceDecodeKey = "oik6PdDdMnOXemTbwvMn9de/h9lFnfBaCWbGMMZqqoSaQaqUOqjVGm5NqsmjcBI1x+sS9ugjB55HEJWRiFXYFw==";
    private static final String defaultVoiceName = "zh-CN-XiaoxiaoMultilingualNeural";
    private static final String defaultRate = "0";
    private static final String defaultPitch = "0";
    private static final String defaultOutputFormat = "audio-24khz-48kbitrate-mono-mp3";

    static {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> voiceListCache = null, cacheDuration, cacheDuration, TimeUnit.MILLISECONDS);
    }

    // 其他方法将在这里实现
    public static JSONObject getEndpoint() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        String signature = sign(endpointURL);
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept-Language", "zh-Hans");
        headers.put("X-ClientVersion", clientVersion);
        headers.put("X-UserId", userId);
        headers.put("X-HomeGeographicRegion", homeGeographicRegion);
        headers.put("X-ClientTraceId", clientTraceId);
        headers.put("X-MT-Signature", signature);
        headers.put("User-Agent", userAgent);
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Content-Length", "0");
        headers.put("Accept-Encoding", "gzip");

        URL url = new URL(endpointURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        connection.setDoOutput(true);
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            System.out.println(IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8));
            throw new IOException("HTTP error code: " + responseCode);
        }

        String response = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
        return new JSONObject(response);
    }
    private static String sign(String urlStr) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        String[] urlParts = urlStr.split("://", 2);
        String u = urlParts[1];
        String encodedUrl = URLEncoder.encode(u, StandardCharsets.UTF_8.toString());

        String uuidStr = UUID.randomUUID().toString().replace("-", "");

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedDate = sdf.format(new Date()).toLowerCase() + "gmt";

        String bytesToSign = String.format("MSTranslatorAndroidApp%s%s%s", encodedUrl, formattedDate, uuidStr);
        bytesToSign = bytesToSign.toLowerCase();

        byte[] decodedKey = Base64.getDecoder().decode(voiceDecodeKey);

        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(decodedKey, "HmacSHA256");
        sha256HMAC.init(secretKey);

        byte[] signatureBytes = sha256HMAC.doFinal(bytesToSign.getBytes(StandardCharsets.UTF_8));
        String signBase64 = Base64.getEncoder().encodeToString(signatureBytes);

        return String.format("MSTranslatorAndroidApp::%s::%s::%s", signBase64, formattedDate, uuidStr);
    }
    public static byte[] getVoice(String text, String voiceName, String rate, String pitch, String outputFormat) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        if (voiceName == null || voiceName.isEmpty()) {
            voiceName = defaultVoiceName;
        }
        if (rate == null || rate.isEmpty()) {
            rate = defaultRate;
        }
        if (pitch == null || pitch.isEmpty()) {
            pitch = defaultPitch;
        }
        if (outputFormat == null || outputFormat.isEmpty()) {
            outputFormat = defaultOutputFormat;
        }

        JSONObject endpoint = getEndpoint();

        String u = String.format("https://%s.tts.speech.microsoft.com/cognitiveservices/v1", endpoint.getString("r"));
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", endpoint.getString("t"));
        headers.put("Content-Type", "application/ssml+xml");
        headers.put("X-Microsoft-OutputFormat", outputFormat);

        String ssml = getSsml(text, voiceName, rate, pitch);

        URL url = new URL(u);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        connection.setDoOutput(true);
        connection.getOutputStream().write(ssml.getBytes(StandardCharsets.UTF_8));

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + responseCode);
        }

        return IOUtils.toByteArray(connection.getInputStream());
    }

    private static String getSsml(String text, String voiceName, String rate, String pitch) {
        return String.format(
                "<speak xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"http://www.w3.org/2001/mstts\" version=\"1.0\" xml:lang=\"zh-CN\">\n" +
                        "  <voice name=\"%s\">\n" +
                        "    <mstts:express-as style=\"general\" styledegree=\"1.0\" role=\"default\">\n" +
                        "      <prosody rate=\"%s%%\" pitch=\"%s%%\" volume=\"50\">%s</prosody>\n" +
                        "    </mstts:express-as>\n" +
                        "  </voice>\n" +
                        "</speak>",
                voiceName, rate, pitch, text
        );
    }

    public static void main(String[] args) throws Exception {
        byte[] xiaoyans = getVoice("忽如一夜春风来，千树万树梨花开", "xiaoyan", null, null, null);
        // 定义文件路径和名称
        File file = new File("D:\\output.mp3");

        // 将 byte[] 写入文件
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(xiaoyans);
            System.out.println("MP3 文件已保存到 D 盘.");
        } catch (IOException e) {
            System.out.println("写入文件时发生错误: " + e.getMessage());
        }
    }
}