package com.lzt.tts.edgeTts.controller;
import cn.hutool.core.io.IoUtil;
import io.github.whitemagic2014.tts.TTS;
import io.github.whitemagic2014.tts.TTSVoice;
import io.github.whitemagic2014.tts.bean.Voice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/tts")
public class EdgeTtsController {

    /**
     *
     * @param t 文本内容
     * @param v 发音人
     * @param r 语速
     * @param p 音调
     * @param o 音频格式
     * @param response
     * @throws Exception
     */
    @RequestMapping("/index")
    public void textToVoice(@RequestParam("t") String t,
                            @RequestParam("v") String v,
                            @RequestParam("r") String r,
                            @RequestParam("p") int p,
                            @RequestParam("o") String o,
                            HttpServletResponse response) throws Exception{
        log.info("textToVoice: t={}, v={}, r={}, p={}, o={}", t, v, r, p, o);
        Voice voice = getVoice(v);
        String path =getFilePath();
        String filename = new TTS(voice, t).voiceRate(r).voiceVolume("100") .storage(path).formatMp3().trans();
        path= path + filename;
        File audioFile = new File(path);
        File audioFile_tt = new File(path+".vtt");
        if (!audioFile.exists()) {
            throw new RuntimeException("当前下载的文件不存在，请检查路径是否正确");
        }
        OutputStream out =null;
        InputStream in = null;
        try  {
            in = new FileInputStream(path);
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(audioFile.getName(), "UTF-8"));
            response.addHeader("Content-Length", "" + audioFile.length());
            response.setContentType("application/octet-stream");
            out = response.getOutputStream();
            IoUtil.copy(in, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            out.flush();
            out.close();
            in.close();
            audioFile.delete();
            audioFile_tt.delete();
        }
    }

    private String getFilePath() {
        String osName = System.getProperty("os.name").toLowerCase();
        String path = "/root/java/";
        if(osName.contains("win")){
            path ="D:/";
        }
        return path
    }

    private Voice getVoice(String v) {
        Voice voice = null;
        List<Voice> voices = TTSVoice.provides();
        for ( Voice vv : voices){
            if(vv.getShortName().equals(v)){
                voice = vv;
                break;
            }
        }
        if(voice == null){
            getVoice("zh-CN-XiaoxiaoNeural");
        }
        return voice;
    }


}
