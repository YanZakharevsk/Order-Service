package com.innowise.order_service.controller;

import com.innowise.order_service.dto.mapper.ItemMapper;
import com.innowise.order_service.dto.request.CreateItemRequest;
import com.innowise.order_service.dto.request.UpdateItemRequest;
import com.innowise.order_service.dto.response.ItemResponse;
import com.innowise.order_service.dto.response.PageResponse;
import com.innowise.order_service.service.ItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    public ItemController(ItemService itemService, ItemMapper itemMapper) {
        this.itemService = itemService;
        this.itemMapper = itemMapper;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody CreateItemRequest request){
        ItemResponse itemResponse = itemService.createItem(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemResponse);
    }

    @GetMapping("/{itemId}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long itemId){
        ItemResponse itemResponse = itemService.getItemById(itemId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(itemResponse);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<PageResponse<ItemResponse>> getAllItems(
            @RequestParam(required = false) String name,
            @RequestParam(required = false)BigDecimal startPrice,
            @RequestParam(required = false)BigDecimal finishPrice,
            @Min(value = 0,message = "Page number can not less than 0")
            @RequestParam(defaultValue = "0") int page,
            @Min(value = 1, message = "Size number can not less than 1")
            @Max(value = 100, message = "Size number can not more than 100")
            @RequestParam(defaultValue = "10") int size
            ){

        ItemSearchCriteria criteria = new ItemSearchCriteria();
        criteria.setName(name);
        criteria.setStartPrice(startPrice);
        criteria.setFinishPrice(finishPrice);
        criteria.setPage(page);
        criteria.setSize(size );
        PageResponse<ItemResponse> pageResponse = itemService.getAllItems(itemMapper.toFilter(criteria), criteria.getPage(), criteria.getSize());

        return ResponseEntity.status(HttpStatus.OK)
                .body(pageResponse);

    }

    @PutMapping("/{itemId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ItemResponse> updateItemById(@PathVariable Long itemId, @Valid @RequestBody UpdateItemRequest request){
        ItemResponse itemResponse = itemService.updateItem(itemId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(itemResponse);
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId){
        itemService.deleteItem(itemId);

        return ResponseEntity.noContent().build();
    }
}
