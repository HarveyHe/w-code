package com.harvey.common.core.bean;

import java.awt.image.BufferedImage;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.CookieGenerator;
import org.springframework.web.util.WebUtils;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.harvey.common.core.config.Config;

/**
 * 验证码组件
 * 
 * @author admin
 * 
 */

public class KaptchaBean implements Controller, InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private Properties defCodeConfig;

    private Boolean isDynamic = true;

    private String sessionType;

    private static final PasswordEncoder encoder = new StandardPasswordEncoder("2F30064B045645D89B3B38936C91658D");

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Producer producer = this.createProducer(request.getParameter("width"), request.getParameter("height"));
        String text = producer.createText();
        this.setEnValue(request, response, text);

        // output
        response.setDateHeader("Expires", 0L);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/png");
        // System.out.println(text);
        BufferedImage image = producer.createImage(text);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "png", out);
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.defCodeConfig == null) {
            try {
                com.google.code.kaptcha.util.Config cfg = this.applicationContext.getBean(com.google.code.kaptcha.util.Config.class);
                if (cfg != null) {
                    this.defCodeConfig = cfg.getProperties();
                }
            } catch (Exception ex) {

            }
        }
        if (this.defCodeConfig == null) {
            this.defCodeConfig = this.createDefaultConfig();
        }
        if (StringUtils.isEmpty(this.sessionType)) {
            this.sessionType = "cookie";
        }
    }

    public void setDefCodeConfig(Properties defCodeConfig) {
        this.defCodeConfig = defCodeConfig;
    }

    private Producer createProducer(String width, String height) {
        DefaultKaptcha producer = new DefaultKaptcha();
        producer.setConfig(this.createConfig(this.defCodeConfig, width, height));
        return producer;
    }

    private com.google.code.kaptcha.util.Config createConfig(Properties codeConfig, String width, String height) {
        Properties prop = new Properties(codeConfig);
        if (isDynamic) {
            // 动态改变背景和边框
            prop.setProperty(Constants.KAPTCHA_BACKGROUND_CLR_FROM, getRandomVal() + "," + getRandomVal() + "," + getRandomVal());
            prop.setProperty(Constants.KAPTCHA_BACKGROUND_CLR_TO, getRandomVal() + "," + getRandomVal() + "," + getRandomVal());

            prop.setProperty(Constants.KAPTCHA_NOISE_COLOR, getRandomVal() + "," + getRandomVal() + "," + getRandomVal());
            prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_COLOR, getRandomVal() + "," + getRandomVal() + "," + getRandomVal());
        }
        if (StringUtils.isNumeric(width)) {
            prop.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, width);
        }
        if (StringUtils.isNumeric(height)) {
            prop.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, height);
        }
        return new com.google.code.kaptcha.util.Config(prop);
    }

    private String getRandomVal() {
        return String.valueOf(RandomUtils.nextInt(0, 255));
    }

    private Properties createDefaultConfig() {
        Properties prop = new Properties();
        prop.setProperty(Constants.KAPTCHA_BORDER, Config.get(Constants.KAPTCHA_BORDER, "yes")); // 是否显示边框
        prop.setProperty(Constants.KAPTCHA_BORDER_COLOR, Config.get(Constants.KAPTCHA_BORDER_COLOR, "105,179,90"));// 边框颜色RGB

        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_COLOR, Config.get(Constants.KAPTCHA_TEXTPRODUCER_FONT_COLOR, "blue"));// 字体颜色RGB
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, Config.get(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "4")); // 文本长度
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_NAMES, Config.get(Constants.KAPTCHA_TEXTPRODUCER_FONT_NAMES, "宋体,微软雅黑,Arial")); // 字体
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE, Config.get(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE, "50")); // 字体大小

        prop.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, Config.get(Constants.KAPTCHA_IMAGE_WIDTH, "200")); // 图片宽度
        prop.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, Config.get(Constants.KAPTCHA_IMAGE_HEIGHT, "50")); // 图片高度
        prop.setProperty(Constants.KAPTCHA_SESSION_KEY, Config.get(Constants.KAPTCHA_SESSION_KEY, "securitycode")); // 会话保存key
        return prop;
    }

    private void setEnValue(HttpServletRequest request, HttpServletResponse response, String rawValue) {
        String enValue = encoder.encode(rawValue);
        String key = this.defCodeConfig.getProperty(Constants.KAPTCHA_SESSION_KEY);
        if ("cookie".equalsIgnoreCase(sessionType)) {
            CookieGenerator cg = new CookieGenerator();
            // cg.setCookieHttpOnly(true);
            cg.setCookieName(key);
            // cg.setCookieSecure(true);
            cg.addCookie(response, enValue);
        } else {
            WebUtils.setSessionAttribute(request, key, enValue);
        }
    }

    private String getEnValue(HttpServletRequest request, HttpServletResponse response) {
        String key = this.defCodeConfig.getProperty(Constants.KAPTCHA_SESSION_KEY);
        if ("cookie".equalsIgnoreCase(sessionType)) {
            Cookie cookie = WebUtils.getCookie(request, key);
            if (cookie != null) {
                cookie.setMaxAge(0);
                response.addCookie(cookie);
                return cookie.getValue();
            }
            return null;
        }
        HttpSession session = request.getSession();
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(key);
        session.removeAttribute(key);
        return (String) value;
    }

    public void setIsDynamic(Boolean isDynamic) {
        this.isDynamic = isDynamic;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public boolean isValid(HttpServletRequest request, HttpServletResponse response, String input) {
        try {
            String enValue = this.getEnValue(request, response);
            return encoder.matches(input, enValue);
        } catch (Exception ex) {
            return false;
        }
    }
}
