package net.xdclass.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;


public class AlipayConfig {

    /**
     * 支付宝网关地址  TODO
     */
    public static final  String PAY_GATEWAY="https://openapi-sandbox.dl.alipaydev.com/gateway.do";


    /**
     * 支付宝 APPID TODO
     */
    public static final  String APPID="9021000140666027";

    /**
     * 应用私钥 TODO
     */
    public static final String APP_PRI_KEY = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCYQa0bgDefbcsGMCPHGKsRiBcFUUE9CmkZPzPBKv2J276gMCLaElz4cgA0idYc4gHAfigf7DEUFZbd7ILTk/ylC57MqziXUxppUiNjlnrED0mjknyrT37vIgATRUHFID9hNTObTLLtxNq7WYRPX5LJ+3fpGGN/CO/07hc/MjHSLV9jc1NTIN+BX7PBexm8QJ1EkWbQwPbCbIwjfel/KYessukFaGb5QaOKINnBJ1sParkdX/6BCd4Rh7nXjuAAnDNoRFYHEYw0xGwQV8wEVOFvCHenP36tVHHPJ2kNPm2wzBKP2Cg3htFATUjKVJPV3DSnYtlwOFKyilfUiXrTc6GHAgMBAAECggEAFp5rIRLDMKQW7SxppEI957TX9qpDjtXlFyMUhTXlxH0orJN6GftwZFhLP4LalB/fMsGLJHLqN5mdeAqSxOvPNJWtWap2FcT6v3q8VycbxdSfk/VHIAwHR289o58+ThKkV8SXjhKu4jPEauC5jzEblXYOMkq1PUIOo8xbF8BVNmvcRlcvQhHNUrHZ7vz60GofTvDcHaR29LYvDfmQD7w0M4i75mB/C8WQfnzqy1oFtBHj+XOX0QnYqytitMRvSTcyvcfgffYMi1KyPP21Vylg4d++7jGpaP3ZMh5ZLrs1Nt7kI8nB5ljybj1XsWBKiQhUzGibwfAet858w4jKxRWnQQKBgQDXWIzDvThfhCMI8TlIZDCKZZ2eWfeUxsXNCClDPo8UD6j+mfWnh8BZCPiEFa+b0ksPbYrnWDDYO3WDMk1jKXwQRTAxq//XLD+b66miVeYEKV11ujzdXjJ0IJADlvzZQpuxlM7IYxQ+vwpZEU3MmCaKiYodSWz7gLuYZSzojI1/2wKBgQC1ABTm2Wdb5opgU4vQkjNrnK/CMVrTivooYmnyXbrz75PsIDyLX7bRp7hkmQpV5uO2AoM6zD+hXaR3AncFa+KcK2yJK/0G0xvpLv0xwdG6sEz1W9nU/HvBGeIyAI2fvB9C1GMLr4+UoT27YOOL4vjTAsd7S7brSQUb7/OmUPwaxQKBgQCoAaPAyo2Cp5qVzWz9d30PXHv+IP6xieqkLavTlKkX46fbCs7G53g/fmo00p5xGT4KSVJHb1ycNrdVphcOOD1cjD4vYpt1ikYOJWvxBMcxk/wgby4xHt6KDyWMR235KqhBgXFakUIoOe+e0Ys7BbF8ABZLBxAJn8O7/6NrwsxaZQKBgQCQJCuBF/s+7Z6fTYlXpUZ72YEClkltlAzZ4l3bHJfIsa9MaPOuTqAJ4JZwzouzkzceeGvHhGbb+/YArJ7aW2tQ0SgTKUvMhoyAq+IJIQADu2jeMLKN8jAfvJwtE9G1NpxynS7vXHVseOfvdB5iBXQAnwL7hnV6dGE0OWgAuBe8vQKBgQCj1Hyf67TEQXhByPVeSh1Ej5Nt38vIXn7+U7dl5tHrn0xuFbtVR6zg3mQ3BHUAbjoS8szRJlOpZ0QL2W1RdUC3yJmeXhbzI3cDp8cAnQWKBVNezqKhe9QT5vjy8tKBcRYe/FIwIE5awbS5OSuNnyrHaic2cyGX2rVshzYrtLw1/Q==";

    /**
     * 支付宝公钥 TODO
     */
    public static final String ALIPAY_PUB_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq0BbE2EmrU82B3DXrTmam/ao5STkovla4/UszEEXEg4UyEkFXjFuFVPxt49988KgnsUt6q0CwYcpYDksa+cu12cIMQFI5vj1Rmy0SUhhQVDbQ3Z0/sPDZQWEmDlFGyZnTKDwErWB0P5PABgOj/3b0iSootOD8Fdr+wEqDRtXm7s8ToBthyoDVIUp0TA2RdKqxWLwmL4pohGBBe5P4Erm3FJU5QJoXdkIhYKx4v6w5FA4dviP+joWau7nCzxuRY3K80TVcIFXRzNFbio7STONO82anIbc6q+SC/mfo4+5e8WumnTnQr/WTMUjnMcc+eZwZVJYPEY0VbD7cFFdna1JgwIDAQAB";

    /**
     * 签名类型
     */
    public static final  String SIGN_TYPE="RSA2";


    /**
     * 字符编码
     */
    public static final  String CHARSET="UTF-8";


    /**
     * 返回参数格式
     */
    public static final  String FORMAT="json";


    /**
     * 构造函数私有化
     */
    private AlipayConfig(){

    }


    private volatile static AlipayClient instance = null;


    /**
     * 单例模式获取, 双重锁校验
     * @return
     */
    public static AlipayClient getInstance(){

        if(instance==null){
            synchronized (AlipayConfig.class){
                if(instance == null){
                    instance = new DefaultAlipayClient(PAY_GATEWAY,APPID,APP_PRI_KEY,FORMAT,CHARSET,ALIPAY_PUB_KEY,SIGN_TYPE);
                }
            }
        }
        return instance;
    }




}
