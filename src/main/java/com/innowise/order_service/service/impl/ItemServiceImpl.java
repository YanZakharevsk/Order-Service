package com.innowise.order_service.service.impl;

import com.innowise.order_service.dto.mapper.ItemMapper;
import com.innowise.order_service.dto.request.CreateItemRequest;
import com.innowise.order_service.dto.request.UpdateItemRequest;
import com.innowise.order_service.dto.response.ItemResponse;
import com.innowise.order_service.dto.response.PageResponse;
import com.innowise.order_service.exception.ItemNotFoundException;
import com.innowise.order_service.jpa.entity.Item;
import com.innowise.order_service.jpa.repository.ItemRepository;
import com.innowise.order_service.service.ItemService;
import com.innowise.order_service.service.filter.ItemFilter;
import com.innowise.order_service.service.specification.ItemSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {


    private  ItemMapper itemMapper;
    private final ItemRepository itemRepository;

    public ItemServiceImpl(ItemMapper itemMapper, ItemRepository itemRepository) {
        this.itemMapper = itemMapper;
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional
    public ItemResponse createItem(CreateItemRequest request) {
        Item item = itemMapper.toEntity(request);

        itemRepository.save(item);
        return itemMapper.toResponse(item);
    }

    @Override
    public ItemResponse getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));

        return itemMapper.toResponse(item);
    }

    @Override
    public PageResponse<ItemResponse> getAllItems(ItemFilter itemFilter, int page, int size) {

        Specification<Item> specification = ItemSpecification.filterBy(itemFilter);

        Page<Item> itemPage = itemRepository.findAll(
                specification,
                PageRequest.of(page, size)
        );

        List<ItemResponse> content = itemPage.getContent()
                .stream()
                .map(item -> itemMapper.toResponse(item))
                .toList();

        return new PageResponse<>(
                content,
                itemPage.getNumber(),
                itemPage.getSize(),
                itemPage.getTotalElements()
        );
    }

    @Override
    @Transactional
    public ItemResponse updateItem(Long itemId, UpdateItemRequest request) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));

        item = itemMapper.update(item, request);
        itemRepository.save(item);

        return itemMapper.toResponse(item);
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));

        itemRepository.delete(item);
    }
}
