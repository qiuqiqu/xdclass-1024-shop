package net.xdclass.controller;

import com.google.code.kaptcha.Producer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.enums.SendCodeEnum;
import net.xdclass.service.NotifyService;
import net.xdclass.util.JsonData;
import net.xdclass.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Api(tags = "通知模块")
@RestController
@RequestMapping("/api/user/v1")
@Slf4j
public class NotifyController {
    @Autowired
    private Producer captchaProducer;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private NotifyService notifyService;


    /**
     * redis key 图形验证码  临时使用10分钟有效
     */
    private static final long CAPTCHA_CODE_EXPIRED = 60 * 1000 * 10;


    /**
     * 获取图形验证码
     *
     * @param request
     * @param response
     */
    @ApiOperation("获取图形验证码")
    @GetMapping(("/captcha"))
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        String cacheRedisKey = getCaptchaKey(request);
        String captchaText = captchaProducer.createText();
        //存储
        redisTemplate.opsForValue().set(cacheRedisKey, captchaText, CAPTCHA_CODE_EXPIRED, TimeUnit.MILLISECONDS);
        log.info("图形验证码:{}", captchaText);
        BufferedImage bufferedImage = captchaProducer.createImage(captchaText);
        ServletOutputStream outputStream = null;
        try {
            response.setDateHeader("Expires", 0);
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            response.addHeader("Cache-Control", "create_date-check=0, pre-check=0");
            response.setHeader("Pragma", "no-cache");
            outputStream = response.getOutputStream();
            ImageIO.write(bufferedImage, "jpg", outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            log.info("获取图形验证码异常:{}", e);
        }
    }

    /**
     * 发送邮箱验证码
     * 1. 比配图形验证码
     * 2. 发送验证码
     *
     * @param to
     * @param captcha
     * @param request
     * @return
     */
    @ApiOperation("发送邮箱注册验证码")
    @GetMapping("/send_code")
    public JsonData sendRegisterCode(@ApiParam("收信人") @RequestParam(value = "to", required = true) String to,
                                     @ApiParam("图形验证码") @RequestParam(value = "captcha", required = true) String captcha,
                                     HttpServletRequest request) {
        String cacheRedisKey = getCaptchaKey(request);
        String cacheCaptcha = redisTemplate.opsForValue().get(cacheRedisKey);
        //比配图形验证码
        if(cacheCaptcha!=null&&captcha!=null&&captcha.equalsIgnoreCase(cacheCaptcha)){
            //成功    删除 图形验证码
            redisTemplate.delete(cacheRedisKey);
            //发送邮箱验证码
            notifyService.sendCode(SendCodeEnum.USER_REGISTER,to);
        }else {
            //失败
            return JsonData.buildResult(BizCodeEnum.CODE_CAPTCHA_ERROR);
        }

        return null;
    }

    private String getCaptchaKey(HttpServletRequest request) {
        String ip = CommonUtil.getIpAddr(request);
        String userAgent = request.getHeader("User-Agent");
        String key = "user-server:captcha" + CommonUtil.MD5(ip + userAgent);
        log.info("ip={}", ip);
        log.info("userAgent={}", userAgent);
        log.info("key={}", key);
        return key;
    }
}
