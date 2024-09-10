package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.enums.StockTaskStateEnum;
import net.xdclass.exception.BizException;
import net.xdclass.mapper.ProductMapper;
import net.xdclass.mapper.ProductTaskMapper;
import net.xdclass.model.ProductDO;
import net.xdclass.model.ProductTaskDO;
import net.xdclass.request.LockProductRequest;
import net.xdclass.request.OrderItemRequest;
import net.xdclass.service.ProductService;
import net.xdclass.util.JsonData;
import net.xdclass.vo.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Version 1.0
 **/

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    ProductTaskMapper productTaskMapper;
    /**
     * 商品分页
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Map<String, Object> page(int page, int size) {


        Page<ProductDO> pageInfo = new Page<>(page, size);

        IPage<ProductDO> productDOIPage = productMapper.selectPage(pageInfo, null);

        Map<String, Object> pageMap = new HashMap<>(3);

        pageMap.put("total_record", productDOIPage.getTotal());
        pageMap.put("total_page", productDOIPage.getPages());
        pageMap.put("current_data", productDOIPage.getRecords().stream().map(obj -> beanProcess(obj)).collect(Collectors.toList()));

        return pageMap;
    }


    /**
     * 根据id找商品详情
     *
     * @param productId
     * @return
     */
    @Override
    public ProductVO findDetailById(long productId) {

        ProductDO productDO = productMapper.selectById(productId);

        return beanProcess(productDO);

    }

    /**
     * 批量查询
     *
     * @param productIdList
     * @return
     */
    @Override
    public List<ProductVO> findProductsByIdBatch(List<Long> productIdList) {

        List<ProductDO> productDOList = productMapper.selectList(new QueryWrapper<ProductDO>().in("id", productIdList));

        List<ProductVO> productVOList = productDOList.stream().map(obj -> beanProcess(obj)).collect(Collectors.toList());

        return productVOList;
    }

    /**
     * 商品库存锁定
     * <p>
     * 1)遍历商品，锁定每个商品购买数量
     * 2)每一次锁定的时候，都要发送延迟消息
     *
     * @param lockProductRequest
     * @return
     */
    @Override
    public JsonData lockProductStock(LockProductRequest lockProductRequest) {
        String orderOutTradeNo = lockProductRequest.getOrderOutTradeNo();
        List<OrderItemRequest> orderItemList = lockProductRequest.getOrderItemList();

        //获取每个商品id
        List<Long> productIdList = orderItemList.stream().map(OrderItemRequest::getProductId).collect(Collectors.toList());
        //根据商品 id 批量查询 商品
        List<ProductVO> productVOList = this.findProductsByIdBatch(productIdList);
        // 将产品信息按产品ID进行分组
        Map<Long, ProductVO> productMap = productVOList.stream().collect(Collectors.toMap(ProductVO::getId, Function.identity()));

        for (OrderItemRequest orderItem : orderItemList) { //遍历订单中的每个商品项。遍历一个商品 然后 锁定商品库存
            //锁定商品库存
            int rows = productMapper.lockProductStock(orderItem.getProductId(), orderItem.getBuyNum());

            if (rows==1){ //将商品的锁定的信息插入 product_task 表中
                ProductVO productVO = productMap.get(orderItem.getProductId());
                ProductTaskDO productTaskDO = new ProductTaskDO();

                //商品 id
                productTaskDO.setProductId(orderItem.getProductId());
                //锁定商品的 数量
                productTaskDO.setBuyNum(orderItem.getBuyNum());
                //商品名称
                productTaskDO.setProductName(productVO.getTitle());
                // LOCK
                productTaskDO.setLockState(StockTaskStateEnum.LOCK.name());
                //商品订单
                productTaskDO.setOutTradeNo(orderOutTradeNo);

                int insert = productTaskMapper.insert(productTaskDO);

                // 发送MQ延迟消息，介绍商品库存  TODO


            }else {
                throw new BizException(BizCodeEnum.ORDER_CONFIRM_LOCK_PRODUCT_FAIL);
            }
        }
        return JsonData.buildSuccess();
    }


    private ProductVO beanProcess(ProductDO productDO) {

        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(productDO, productVO);
        productVO.setStock(productDO.getStock() - productDO.getLockStock());
        return productVO;
    }
}
