package com.innowise.order_service.service;

import com.innowise.order_service.dto.request.CreateItemRequest;
import com.innowise.order_service.dto.request.UpdateItemRequest;
import com.innowise.order_service.dto.response.ItemResponse;
import com.innowise.order_service.dto.response.PageResponse;
import com.innowise.order_service.service.filter.ItemFilter;

/**
 * Service interface for managing catalog item operations in the system.
 * Provides methods for item creation, retrieval, updates, deletion, and paginated searches.
 */
public interface ItemService {

    /**
     * Creates a new item in the store catalog.
     *
     * @param request the data transfer object containing the new item's properties (e.g., name, price)
     * @return an {@link ItemResponse} containing the created item's data
     */
    ItemResponse createItem(CreateItemRequest request);

    /**
     * Retrieves a specific item by its unique identifier.
     *
     * @param itemId the unique identifier of the item to retrieve
     * @return an {@link ItemResponse} containing the item's details
     * @throws RuntimeException if the item with the specified ID is not found (e.g., EntityNotFoundException)
     */
    ItemResponse getItemById(Long itemId);

    /**
     * Retrieves a paginated list of catalog items matching the provided filter criteria.
     *
     * @param itemFilter the criteria used to filter items (e.g., by title, price threshold)
     * @param page       the zero-based page index to retrieve
     * @param size       the maximum number of items per page
     * @return a {@link PageResponse} containing a list of {@link ItemResponse} objects and pagination details
     */
    PageResponse<ItemResponse> getAllItems(ItemFilter itemFilter, int page, int size);

    /**
     * Updates an existing catalog item's properties.
     *
     * @param itemId  the unique identifier of the item to update
     * @param request the data transfer object containing updated fields for the item
     * @return an {@link ItemResponse} reflecting the updated item details
     * @throws RuntimeException if the item with the specified ID is not found
     */
    ItemResponse updateItem(Long itemId, UpdateItemRequest request);

    /**
     * Permanently or logically deletes an item from the catalog.
     *
     * @param itemId the unique identifier of the item to delete
     * @throws RuntimeException if the item with the specified ID is not found
     */
    void deleteItem(Long itemId);
}