package net.xdclass.component;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.enums.ProductOrderPayTypeEnum;
import net.xdclass.vo.PayInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class PayFactory {


    @Autowired
    private AlipayStrategy alipayStrategy;

    @Autowired
    private WechatPayStrategy wechatPayStrategy;


    /**
     * 创建支付，简单工程模式
     * @param payInfoVO
     * @return
     */
    public String pay(PayInfoVO payInfoVO){

        String payType = payInfoVO.getPayType();

        if(ProductOrderPayTypeEnum.ALIPAY.name().equalsIgnoreCase(payType)){
            //支付宝支付
            PayStrategyContext payStrategyContext = new PayStrategyContext(alipayStrategy);

            return payStrategyContext.executeUnifiedorder(payInfoVO);

        } else if(ProductOrderPayTypeEnum.WECHAT.name().equalsIgnoreCase(payType)){
            //微信支付 暂未实现
            PayStrategyContext payStrategyContext = new PayStrategyContext(wechatPayStrategy);

            return payStrategyContext.executeUnifiedorder(payInfoVO);
        }


        return "";
    }


    /**
     * 查询订单支付状态
     *
     * 支付成功返回非空，其他返回空
     *
     * @param payInfoVO
     * @return
     */
    public String queryPaySuccess(PayInfoVO payInfoVO){
        String payType = payInfoVO.getPayType();

        if(ProductOrderPayTypeEnum.ALIPAY.name().equalsIgnoreCase(payType)){
            //支付宝支付
            PayStrategyContext payStrategyContext = new PayStrategyContext(alipayStrategy);

            return payStrategyContext.executeQueryPaySuccess(payInfoVO);

        } else if(ProductOrderPayTypeEnum.WECHAT.name().equalsIgnoreCase(payType)){
            //微信支付 暂未实现
            PayStrategyContext payStrategyContext = new PayStrategyContext(wechatPayStrategy);

            return payStrategyContext.executeQueryPaySuccess(payInfoVO);
        }


        return "";


    }




}
