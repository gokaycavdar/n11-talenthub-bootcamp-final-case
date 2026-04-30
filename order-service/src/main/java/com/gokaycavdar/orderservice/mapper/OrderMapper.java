package com.gokaycavdar.orderservice.mapper;

import com.gokaycavdar.orderservice.dto.order.OrderItemResponse;
import com.gokaycavdar.orderservice.dto.order.OrderResponse;
import com.gokaycavdar.orderservice.entity.Order;
import com.gokaycavdar.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    OrderResponse toOrderResponse(Order order);
}
