package net.xdclass.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.request.ConfirmOrderRequest;
import net.xdclass.service.ProductOrderService;
import net.xdclass.util.JsonData;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class ProductOrderServiceImpl implements ProductOrderService {
    @Override
    public JsonData confirmOrder(ConfirmOrderRequest orderRequest) {
        return null;
    }
}
