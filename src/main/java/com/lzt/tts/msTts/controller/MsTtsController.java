package com.lzt.tts.msTts.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@Controller
@RequestMapping("/mstts")
public class MsTtsController {

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
                            HttpServletResponse response) throws Exception {
        log.info("textToVoice: t={}, v={}, r={}, p={}, o={}", t, v, r, p, o);

    }
}
