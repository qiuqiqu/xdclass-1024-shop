package net.xdclass.component;

import net.xdclass.vo.PayInfoVO;



public interface PayStrategy {


    /**
     * 下单
     * @return
     */
    String unifiedorder(PayInfoVO payInfoVO);


    /**
     *  退款
     * @param payInfoVO
     * @return
     */
    default String refund(PayInfoVO payInfoVO){return "";}


    /**
     * 查询支付是否成功
     * @param payInfoVO
     * @return
     */
    default String queryPaySuccess(PayInfoVO payInfoVO){return "";}


}
