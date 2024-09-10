package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.RabbitMQConfig;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.enums.CouponStateEnum;
import net.xdclass.enums.ProductOrderStateEnum;
import net.xdclass.enums.StockTaskStateEnum;
import net.xdclass.exception.BizException;
import net.xdclass.feign.ProductOrderFeignSerivce;
import net.xdclass.interceptor.LoginInterceptor;
import net.xdclass.mapper.CouponRecordMapper;
import net.xdclass.mapper.CouponTaskMapper;
import net.xdclass.model.CouponRecordDO;
import net.xdclass.model.CouponRecordMessage;
import net.xdclass.model.CouponTaskDO;
import net.xdclass.model.LoginUser;
import net.xdclass.request.LockCouponRecordRequest;
import net.xdclass.service.CouponRecordService;
import net.xdclass.util.JsonData;
import net.xdclass.vo.CouponRecordVO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Version 1.0
 **/

@Service
@Slf4j
public class CouponRecordServiceImpl implements CouponRecordService {


    @Autowired
    private CouponRecordMapper couponRecordMapper;

    @Autowired
    private CouponTaskMapper couponTaskMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitMQConfig rabbitMQConfig;

    @Autowired
    ProductOrderFeignSerivce productOrderFeignSerivce;


    @Override
    public Map<String, Object> page(int page, int size) {

        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        //封装分页信息
        Page<CouponRecordDO> pageInfo = new Page<>(page, size);

        IPage<CouponRecordDO> recordDOIPage = couponRecordMapper.selectPage(pageInfo, new QueryWrapper<CouponRecordDO>()
                .eq("user_id", loginUser.getId()).orderByDesc("create_time"));

        Map<String, Object> pageMap = new HashMap<>(3);

        pageMap.put("total_record", recordDOIPage.getTotal());
        pageMap.put("total_page", recordDOIPage.getPages());
        pageMap.put("current_data", recordDOIPage.getRecords().stream().map(obj -> beanProcess(obj)).collect(Collectors.toList()));

        return pageMap;
    }


    @Override
    public CouponRecordVO findById(long recordId) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        CouponRecordDO couponRecordDO = couponRecordMapper.selectOne(new QueryWrapper<CouponRecordDO>()
                .eq("id", recordId).eq("user_id", loginUser.getId()));

        if (couponRecordDO == null) {
            return null;
        }

        return beanProcess(couponRecordDO);
    }


    /**
     * 锁定优惠券
     * <p>
     * 1）锁定优惠券记录
     * 2）task表插入记录
     * 3）发送延迟消息
     *
     * @param recordRequest
     * @return
     */
    @Override
    public JsonData lockCouponRecords(LockCouponRecordRequest recordRequest) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        List<Long> lockCouponRecordIds = recordRequest.getLockCouponRecordIds();
        String orderOutTradeNo = recordRequest.getOrderOutTradeNo();

        //锁定优惠券记录 (批量更新优惠券状态)
        int updateRows = couponRecordMapper.lockUseStateBatch(loginUser.getId(), CouponStateEnum.USED.name(), lockCouponRecordIds);

        //task表插入记录
        List<CouponTaskDO> couponTaskDOList = lockCouponRecordIds.stream().map(obj -> {
            CouponTaskDO couponTaskDO = new CouponTaskDO();
            couponTaskDO.setCouponRecordId(obj);
            couponTaskDO.setCreateTime(new Date());
            couponTaskDO.setOutTradeNo(orderOutTradeNo);
            couponTaskDO.setLockState(StockTaskStateEnum.LOCK.name());
            return couponTaskDO;
        }).collect(Collectors.toList());
        int insertRows = couponTaskMapper.insertBatch(couponTaskDOList);

        log.info("优惠券记录锁定updateRows={}", updateRows);
        log.info("新增优惠券记录task insertRows={}", insertRows);

        if (updateRows == lockCouponRecordIds.size() && insertRows == couponTaskDOList.size()) {
            //发送延迟队列
            for (CouponTaskDO couponTaskDO : couponTaskDOList) {
                CouponRecordMessage couponRecordMessage = new CouponRecordMessage();
                couponRecordMessage.setOutTradeNo(couponTaskDO.getOutTradeNo());
                couponRecordMessage.setTaskId(couponTaskDO.getId());

                rabbitTemplate.convertAndSend(rabbitMQConfig.getEventExchange(),
                        rabbitMQConfig.getCouponReleaseDelayRoutingKey(), couponRecordMessage);
                log.info("优惠券锁定消息发送成功:{}", couponRecordMessage.toString());
            }

            return JsonData.buildSuccess();
        } else {
            throw new BizException(BizCodeEnum.COUPON_RECORD_LOCK_FAIL);
        }
    }

    /**
     * 解锁优惠券记录
     * 1）查询task工作单是否存在
     * 2) 查询订单状态
     *
     * @param recordMessage
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public boolean releaseCouponRecord(CouponRecordMessage recordMessage) {
        CouponTaskDO couponTaskDO = couponTaskMapper.selectOne(new QueryWrapper<CouponTaskDO>().eq("id", recordMessage.getTaskId()));
        if (couponTaskDO == null) {
            //不用处理
            log.info("工作单不存在,消息:{}", recordMessage);
            return true;
        }
        //工作单存在且状态是 LOCK 才处理
        if (couponTaskDO.getLockState().equals(StockTaskStateEnum.LOCK.name())) {
            //工作单的状态是 LOCK
            JsonData jsonData = productOrderFeignSerivce.queryProductOrderState(recordMessage.getOutTradeNo());
            Integer code = jsonData.getCode();
            if (code==0){ //正常响应
                //判断订单状态
                String state = jsonData.getData().toString();
                if (ProductOrderStateEnum.NEW.name().equals(state)){
                    //订单状态是 NEW 新建状态,返回消息队列,重新投递
                    log.info("订单状态是 NEW 新建状态,返回消息队列,重新投递",recordMessage);
                    return false;
                }

                //如果订单状态已支付
                if (ProductOrderStateEnum.PAY.name().equals(state)){
                    //如果订单状态已支付,将 task 的状态由 LOCK 改为 FINISH
                    couponTaskDO.setLockState(StockTaskStateEnum.FINISH.name());
                    couponTaskMapper.update(couponTaskDO,new QueryWrapper<CouponTaskDO>().eq("id",recordMessage.getTaskId()));
                    log.info("订单已经支付，修改库存锁定工作单FINISH状态:{}",recordMessage);
                    return true;
                }
            }

            //订单不存在，或者订单被取消，确认消息,修改task状态为CANCEL,恢复优惠券使用记录为NEW
            log.warn("订单不存在，或者订单被取消，确认消息,修改task状态为CANCEL,恢复优惠券使用记录为NEW,message:{}",recordMessage);
            couponTaskDO.setLockState(StockTaskStateEnum.CANCEL.name());
            couponTaskMapper.update(couponTaskDO,new QueryWrapper<CouponTaskDO>().eq("id",recordMessage.getTaskId()));

            //恢复优惠券记录是NEW状态
            couponRecordMapper.updateState(couponTaskDO.getCouponRecordId(),CouponStateEnum.NEW.name());

            return true;

        } else {
            log.info("工作单状态不是 lock,消息体:{}", couponTaskDO.getLockState(), recordMessage);
            return true;
        }
    }


    private CouponRecordVO beanProcess(CouponRecordDO couponRecordDO) {


        CouponRecordVO couponRecordVO = new CouponRecordVO();
        BeanUtils.copyProperties(couponRecordDO, couponRecordVO);
        return couponRecordVO;
    }
}
