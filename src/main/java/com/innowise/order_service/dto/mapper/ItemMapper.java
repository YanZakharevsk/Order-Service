package com.innowise.order_service.dto.mapper;

import com.innowise.order_service.controller.ItemSearchCriteria;
import com.innowise.order_service.dto.request.CreateItemRequest;
import com.innowise.order_service.dto.request.UpdateItemRequest;
import com.innowise.order_service.dto.response.ItemResponse;
import com.innowise.order_service.jpa.entity.Item;
import com.innowise.order_service.service.ItemService;
import com.innowise.order_service.service.filter.ItemFilter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    Item toEntity(CreateItemRequest request);

    Item update(@MappingTarget Item item, UpdateItemRequest request);

    @Mapping(source = "item.id", target = "itemId")
    ItemResponse toResponse(Item item);

    ItemFilter toFilter(ItemSearchCriteria itemSearchCriteria);
}
