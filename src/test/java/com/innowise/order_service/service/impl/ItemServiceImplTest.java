package com.innowise.order_service.service.impl;

import com.innowise.order_service.dto.mapper.ItemMapper;
import com.innowise.order_service.dto.request.CreateItemRequest;
import com.innowise.order_service.dto.request.UpdateItemRequest;
import com.innowise.order_service.dto.response.ItemResponse;
import com.innowise.order_service.dto.response.PageResponse;
import com.innowise.order_service.exception.ItemNotFoundException;
import com.innowise.order_service.jpa.entity.Item;
import com.innowise.order_service.jpa.repository.ItemRepository;
import com.innowise.order_service.service.filter.ItemFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    @DisplayName("Create Item - Success")
    void createItem_Success() {
        CreateItemRequest request = new CreateItemRequest();
        request.setName("Test Item");
        request.setPrice(BigDecimal.TEN);

        Item item = new Item(1L, "Test Item", BigDecimal.TEN);
        ItemResponse response = new ItemResponse();
        response.setItemId(1L);
        response.setName("Test Item");
        response.setPrice(BigDecimal.TEN);

        when(itemMapper.toEntity(request)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toResponse(item)).thenReturn(response);

        ItemResponse result = itemService.createItem(request);

        assertNotNull(result);
        assertEquals(1L, result.getItemId());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    @DisplayName("Get Item By Id - Success")
    void getItemById_Success() {
        Long itemId = 1L;
        Item item = new Item(itemId, "Test Item", BigDecimal.TEN);
        ItemResponse response = new ItemResponse();
        response.setItemId(itemId);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toResponse(item)).thenReturn(response);

        ItemResponse result = itemService.getItemById(itemId);

        assertNotNull(result);
        assertEquals(itemId, result.getItemId());
    }

    @Test
    @DisplayName("Get Item By Id - Throws ItemNotFoundException")
    void getItemById_NotFound_ThrowsException() {
        Long itemId = 99L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.getItemById(itemId));
    }

    @Test
    @DisplayName("Get All Items - Success with Pagination")
    void getAllItems_Success() {
        ItemFilter filter = new ItemFilter("Test", BigDecimal.ZERO, BigDecimal.TEN);
        Item item = new Item(1L, "Test Item", BigDecimal.TEN);
        Page<Item> itemPage = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);

        ItemResponse response = new ItemResponse();
        response.setItemId(1L);

        when(itemRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(itemPage);
        when(itemMapper.toResponse(item)).thenReturn(response);

        PageResponse<ItemResponse> result = itemService.getAllItems(filter, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Update Item - Success")
    void updateItem_Success() {
        Long itemId = 1L;
        UpdateItemRequest request = new UpdateItemRequest();
        Item existingItem = new Item(itemId, "Old Name", BigDecimal.ONE);
        Item updatedItem = new Item(itemId, "New Name", BigDecimal.TEN);
        ItemResponse response = new ItemResponse();
        response.setItemId(itemId);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemMapper.update(existingItem, request)).thenReturn(updatedItem);
        when(itemRepository.save(updatedItem)).thenReturn(updatedItem);
        when(itemMapper.toResponse(updatedItem)).thenReturn(response);

        ItemResponse result = itemService.updateItem(itemId, request);

        assertNotNull(result);
        verify(itemRepository, times(1)).save(updatedItem);
    }

    @Test
    @DisplayName("Update Item - Throws ItemNotFoundException")
    void updateItem_NotFound_ThrowsException() {
        Long itemId = 99L;
        UpdateItemRequest request = new UpdateItemRequest();
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.updateItem(itemId, request));
    }

    @Test
    @DisplayName("Delete Item - Success")
    void deleteItem_Success() {
        Long itemId = 1L;
        Item item = new Item(itemId, "To Delete", BigDecimal.ONE);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        itemService.deleteItem(itemId);

        verify(itemRepository, times(1)).delete(item);
    }

    @Test
    @DisplayName("Delete Item - Throws ItemNotFoundException")
    void deleteItem_NotFound_ThrowsException() {
        Long itemId = 99L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.deleteItem(itemId));
    }
}