package com.lzt.tts.msTts.utils;
import net.dongliu.requests.RequestBuilder;
import net.dongliu.requests.Requests;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class MsTtsUtils {
    private static String endpointURL          = "https://dev.microsofttranslator.com/apps/endpoint?api-version=1.0";
    private static String voicesListURL        = "https://eastus.api.speech.microsoft.com/cognitiveservices/voices/list";
    private static String userAgent            = "okhttp/4.5.0";
    private static String clientVersion        = "4.0.530a 5fe1dc6c";
    private static String userId               = "0f04d16a175c411e";
    private static String homeGeographicRegion = "zh-Hans-CN";
    private static String clientTraceId        = "aab069b9-70a7-4844-a734-96cd78d94be9";
    private static String voiceDecodeKey       = "oik6PdDdMnOXemTbwvMn9de/h9lFnfBaCWbGMMZqqoSaQaqUOqjVGm5NqsmjcBI1x+sS9ugjB55HEJWRiFXYFw==";
    private static String defaultVoiceName     = "zh-CN-XiaoxiaoMultilingualNeural";
    private static String defaultRate          = "0";
    private static String defaultPitch         = "0";
    private static String defaultOutputFormat  = "audio-24khz-48kbitrate-mono-mp3";

    public static String getSign(String urlStr) throws Exception {
        // 移除URL的协议部分，只保留主机和路径
        String u = urlStr.split("://")[1];
        // URL编码
        String encodedUrl = URLEncoder.encode(u, StandardCharsets.UTF_8.toString());

        // 生成不带短横线的UUID
        String uuidStr = UUID.randomUUID().toString().replace("-", "");
        // 格式化日期为RFC 1123格式并添加"GMT"后缀
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
        // 设置时区为GMT
        sdf.setTimeZone(TimeZone.getTimeZone(ZoneId.of("GMT")));
        // 格式化当前日期，并添加"GMT"后缀
        String formattedDate = sdf.format(new java.util.Date()) + " GMT";

        // 构造待签名字符串
        String bytesToSign = "MSTranslatorAndroidApp" + encodedUrl + formattedDate + uuidStr;
        bytesToSign = bytesToSign.toLowerCase();

        // 解码Base64密钥
        byte[] decodedKey = Base64.getDecoder().decode(voiceDecodeKey);

        // 创建HMAC SHA-256 Mac实例
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        hmacSha256.init(new SecretKeySpec(decodedKey, "HmacSHA256"));

        // 执行签名
        byte[] rawHmac = hmacSha256.doFinal(bytesToSign.getBytes(StandardCharsets.UTF_8));

        // 将签名字节编码为Base64字符串
        String signBase64 = Base64.getEncoder().encodeToString(rawHmac);

        // 返回最终签名字符串
        return String.format("MSTranslatorAndroidApp::%s::%s::%s", signBase64, formattedDate, uuidStr);
    }


    /**
     * 生成 SSML 格式的文本
     *
     * @param text 文本内容
     * @param voiceName 语音名称
     * @param rate 语速，取值范围 0 到 100，作为字符串传递（但通常应转换为浮点数或整数进行验证）
     * @param pitch 音调，取值范围 0 到 100，作为字符串传递（但通常应转换为浮点数或整数进行验证）
     * @return 返回 SSML 格式的文本
     */
    public static String getSsml(String text, String voiceName, String rate, String pitch) {
        StringBuilder ssml = new StringBuilder();
        ssml.append("<speak xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"http://www.w3.org/2001/mstts\" version=\"1.0\" xml:lang=\"zh-CN\">");
        ssml.append("<voice name=\"").append(voiceName).append("\">");
        ssml.append("<mstts:express-as style=\"general\" styledegree=\"1.0\" role=\"default\">");
        ssml.append("<prosody rate=\"").append(rate).append("%\" pitch=\"").append(pitch).append("%\" volume=\"50\">").append(text).append("</prosody>");
        ssml.append("</mstts:express-as>");
        ssml.append("</voice>");
        ssml.append("</speak>");
        return ssml.toString();
    }


    public static Map<String, Object> getEndpoint() throws Exception {
        String signature = getSign(endpointURL);

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

        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        connection.disconnect();

        Gson gson = new Gson();
        return gson.fromJson(response.toString(), new TypeToken<Map<String, Object>>(){}.getType());
    }

    public static byte[] getVoice(String text, String voiceName, String rate, String pitch, String outputFormat) throws Exception {
        if (StringUtils.isEmpty(voiceName)) {
            voiceName = defaultVoiceName;
        }
        if (StringUtils.isEmpty(rate)) {
            rate = defaultRate;
        }
        if (StringUtils.isEmpty(pitch)){
            pitch = defaultPitch;
        }
        if (StringUtils.isEmpty(outputFormat)) {
            outputFormat = defaultOutputFormat;
        }

        Map<String, Object> endpoint = getEndpoint();
        if (endpoint == null) {
            throw new Exception("Failed to get endpoint");
        }

        String u = String.format("https://%s.tts.speech.microsoft.com/cognitiveservices/v1", endpoint.get("r"));
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", (String) endpoint.get("t"));
        headers.put("Content-Type", "application/ssml+xml");
        headers.put("X-Microsoft-OutputFormat", outputFormat);

        String ssml = getSsml(text, voiceName, rate, pitch);

        URL url = new URL(u);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }

        connection.setDoOutput(true);
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(ssml);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }

        try (Scanner scanner = new Scanner(connection.getInputStream());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            while (scanner.hasNext()) {
                outputStream.write(scanner.next().getBytes());
            }
            return outputStream.toByteArray();
        } finally {
            connection.disconnect();
        }
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
