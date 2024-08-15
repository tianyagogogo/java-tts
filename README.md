# java-tts

## 1.编译
```shell
mvn package
```

## 2.运行
```
java -jar nohup tts-1.0-SNAPSHOT.jar
```

## 3.访问

在浏览器中访问：
```
http://ip:20039/tts/tts/index?t=%E5%B2%82%E6%9B%B0%E6%97%A0%E8%A1%A3%EF%BC%9F%E4%B8%8E%E5%AD%90%E5%90%8C%E8%A2%8D%E3%80%82%E7%8E%8B%E4%BA%8E%E5%85%B4%E5%B8%88%EF%BC%8C%E4%BF%AE%E6%88%91%E6%88%88%E7%9F%9B%EF%BC%8C%E4%B8%8E%E5%AD%90%E5%90%8C%E4%BB%87%EF%BC%81%E5%B2%82%E6%9B%B0%E6%97%A0%E8%A1%A3%EF%BC%9F%E4%B8%8E%E5%AD%90%E5%90%8C%E6%B3%BD%E3%80%82%E7%8E%8B%E4%BA%8E%E5%85%B4%E5%B8%88%EF%BC%8C%E4%BF%AE%E6%88%91%E7%9F%9B%E6%88%9F%EF%BC%8C%E4%B8%8E%E5%AD%90%E5%81%95%E4%BD%9C%EF%BC%81%E5%B2%82%E6%9B%B0%E6%97%A0%E8%A1%A3%EF%BC%9F%E4%B8%8E%E5%AD%90%E5%90%8C%E8%A3%B3%E3%80%82%E7%8E%8B%E4%BA%8E%E5%85%B4%E5%B8%88%EF%BC%8C%E4%BF%AE%E6%88%91%E7%94%B2%E5%85%B5%EF%BC%8C%E4%B8%8E%E5%AD%90%E5%81%95%E8%A1%8C!&v=zh-CN-XiaoxiaoNeural&r=1.5&p=0&o=audio-24khz-48kbitrate-mono-mp3
```

阅读APP配置：
```
http://ip:20039/tts/tts/index?t={{java.encodeURI(speakText)}}&v=zh-CN-XiaoxiaoNeural&r={{java.encodeURI(speakSpeed)/10}}&p=0&o=audio-24khz-96kbitrate-mono-mp3
```
