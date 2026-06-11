package com.innowise.order_service.dto.mapper;

import com.innowise.order_service.controller.OrderSearchCriteria;
import com.innowise.order_service.dto.request.CreateOrderRequest;
import com.innowise.order_service.dto.request.UpdateOrderStatusRequest;
import com.innowise.order_service.dto.response.OrderResponse;
import com.innowise.order_service.jpa.entity.Order;
import com.innowise.order_service.kafka.dto.OrderKafkaDto;
import com.innowise.order_service.service.filter.OrderFilter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    Order toEntity(CreateOrderRequest request);

    Order updateStatus(@MappingTarget Order order, UpdateOrderStatusRequest request);

    Order updateOrderItems(@MappingTarget Order order, CreateOrderRequest request);

    OrderFilter toFilter(OrderSearchCriteria criteria);

    @Mapping(source = "totalPrice", target = "paymentAmount")
    @Mapping(source = "user.id", target = "userId")
    OrderKafkaDto responseToKafkaDto(OrderResponse response);
}
