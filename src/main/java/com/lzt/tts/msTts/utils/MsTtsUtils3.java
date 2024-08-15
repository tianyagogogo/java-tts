package com.lzt.tts.msTts.utils;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.sun.corba.se.impl.resolver.SplitLocalResolverImpl;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class MsTtsUtils3 {
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

    // 模拟 Go 中的 logrus.New()
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(MsTtsUtils3.class.getName());

    // 模拟 Go 中的 http.Client
    private static HttpURLConnection client;

    // 模拟 Go 中的 voiceListCache
    private static Object[] voiceListCache;

    // 模拟 Go 中的 cacheDuration
    private static final long cacheDuration = 1 * 60 * 60 * 1000; // 1 小时转换为毫秒

    static {
        // 模拟 Go 中的 ticker
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(cacheDuration);
                    voiceListCache = null;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 模拟 Go 中的 GetEndpoint 函数
    public static Map<String, Object> getEndpoint() {
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

        try {
            URL url = new URL(endpointURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            // 设置请求头
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            int responseCode = connection.getResponseCode();
            if (responseCode!= 200) {
                System.out.println(readBytesFromInputStream(connection.getInputStream()));
                log.severe("Failed to get endpoint. Response code: " + responseCode);
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine())!= null) {
                response.append(line);
            }
            reader.close();

            // 解析 JSON 响应
            JSONObject jsonObject = new JSONObject(response.toString());
            Map<String, Object> result = new HashMap<>();
            for (String key : jsonObject.keySet()) {
                result.put(key, jsonObject.get(key));
            }

            return result;
        } catch (IOException | JSONException e) {
            log.severe("Failed to get endpoint: " + e.getMessage());
            return null;
        }
    }

    // 模拟 Go 中的 Sign 函数
    public static String sign(String urlStr) {
        String[] parts = urlStr.split("://");
        String u = parts[1];
        String encodedUrl = encodeUrl(u);
        String uuidStr = generateUuid().replaceAll("-", "");
        String formattedDate = getFormattedDate();
        String bytesToSign = "MSTranslatorAndroidApp" + encodedUrl + formattedDate + uuidStr;
        bytesToSign = bytesToSign.toLowerCase();

        byte[] decode;
        try {
            decode = Base64.getDecoder().decode(voiceDecodeKey);
        } catch (IllegalArgumentException e) {
            log.severe("Failed to decode voiceDecodeKey: " + e.getMessage());
            return null;
        }

        byte[] secretKey;
        try {
            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            hash.update(decode);
            hash.update(bytesToSign.getBytes());
            secretKey = hash.digest();
        } catch (NoSuchAlgorithmException e) {
            log.severe("Failed to create SHA-256 hash: " + e.getMessage());
            return null;
        }

        String signBase64 = Base64.getEncoder().encodeToString(secretKey);
        return "MSTranslatorAndroidApp::" + signBase64 + "::" + formattedDate + "::" + uuidStr;
    }

    // 辅助函数：对 URL 进行编码
    private static String encodeUrl(String u) {
        try {
            return java.net.URLEncoder.encode(u, "UTF-8");
        } catch (IOException e) {
            log.severe("Failed to encode URL: " + e.getMessage());
            return null;
        }
    }

    // 辅助函数：生成 UUID
    private static String generateUuid() {
        return java.util.UUID.randomUUID().toString();
    }

    // 辅助函数：获取格式化的日期
    private static String getFormattedDate() {
        //return java.time.ZonedDateTime.now().withZoneSameInstant(java.time.ZoneOffset.UTC).format(java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss")).toLowerCase() + "gmt";
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDate = sdf.format(new Date()).toLowerCase() + "gmt";
        return formattedDate;
    }




    // 模拟 Go 中的 GetVoice 函数
    public static byte[] getVoice(String text, String voiceName, String rate, String pitch, String outputFormat) {
        if (voiceName.isEmpty()) {
            voiceName = defaultVoiceName;
        }
        if (StringUtils.isEmpty(rate)) {
            rate = defaultRate;
        }
        if (StringUtils.isEmpty(pitch)) {
            pitch = defaultPitch;
        }
        if (StringUtils.isEmpty(outputFormat)) {
            outputFormat = defaultOutputFormat;
        }

        Map<String, Object> endpoint = getEndpoint();
        System.out.println(endpoint.toString());
        if (endpoint == null) {
            log.severe("Failed to get endpoint");
            return null;
        }

        String u = "https://" + endpoint.get("r") + ".tts.speech.microsoft.com/cognitiveservices/v1";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", endpoint.get("t").toString());
        headers.put("Content-Type", "application/ssml+xml");
        headers.put("X-Microsoft-OutputFormat", outputFormat);

        String ssml = getSsml(text, voiceName, rate, pitch);

        try {
            URL url = new URL(u);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            // 设置请求头
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            connection.setDoOutput(true);
            connection.getOutputStream().write(ssml.getBytes());

            int responseCode = connection.getResponseCode();
            if (responseCode!= 200) {
                log.severe("Failed to get voice. Response code: " + responseCode);
                return null;
            }

            return readBytesFromInputStream(connection.getInputStream());
        } catch (IOException e) {
            log.severe("Failed to get voice: " + e.getMessage());
            return null;
        }
    }

    public static byte[] readBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length))!= -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    // 模拟 Go 中的 GetSsml 函数
    public static String getSsml(String text, String voiceName, String rate, String pitch) {
        return "<speak xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"http://www.w3.org/2001/mstts\" version=\"1.0\" xml:lang=\"zh-CN\">"
                + "<voice name=\"" + voiceName + "\">"
                + "<mstts:express-as style=\"general\" styledegree=\"1.0\" role=\"default\">"
                + "<prosody rate=\"" + rate + "%\" pitch=\"" + pitch + "%\" volume=\"50\">" + text + "</prosody>"
                + "</mstts:express-as>"
                + "</voice>"
                + "</speak>";
    }

    // 模拟 Go 中的 VoiceList 函数
    public static Object[] voiceList() {
        // 如果缓存中有值，直接返回缓存的结果
        if (voiceListCache!= null) {
            return voiceListCache;
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 Edg/107.0.1418.26");
        headers.put("X-Ms-Useragent", "SpeechStudio/2021.05.001");
        headers.put("Content-Type", "application/json");
        headers.put("Origin", "https://azure.microsoft.com");
        headers.put("Referer", "https://azure.microsoft.com");

        try {
            URL url = new URL(voicesListURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 设置请求头
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            int responseCode = connection.getResponseCode();
            if (responseCode!= 200) {
                log.severe("Failed to get voice list. Response code: " + responseCode);
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine())!= null) {
                response.append(line);
            }
            reader.close();

            // 解析 JSON 响应
            JSONObject jsonObject = new JSONObject(response.toString());
            Object[] result = new Object[jsonObject.length()];
            int i = 0;
            for (String key : jsonObject.keySet()) {
                result[i++] = jsonObject.get(key);
            }

            // 将结果存储到缓存中
            voiceListCache = result;

            return result;
        } catch (IOException | JSONException e) {
            log.severe("Failed to get voice list: " + e.getMessage());
            return null;
        }
    }

    // 模拟 Go 中的 ByteCountIEC 函数
    public static String byteCountIEC(long b) {
        final long unit = 1024;
        if (b < unit) {
            return b + " B";
        }
        long div = unit;
        int exp = 0;
        while (b / div >= unit) {
            div *= unit;
            exp++;
        }
        return String.format("%.1f %ciB", (double) b / (double) div, "KMGTPE".charAt(exp));
    }


    public static void main(String[] args) throws Exception {
        byte[] xiaoyans = getVoice("忽如一夜春风来，千树万树梨花开", "xiaoyan", null, null, null);
        System.out.println(xiaoyans);
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