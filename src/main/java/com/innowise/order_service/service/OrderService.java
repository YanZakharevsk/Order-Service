package com.innowise.order_service.service;

import com.innowise.order_service.dto.request.CreateOrderRequest;
import com.innowise.order_service.dto.request.UpdateOrderStatusRequest;
import com.innowise.order_service.dto.response.OrderResponse;
import com.innowise.order_service.dto.response.PageResponse;
import com.innowise.order_service.service.filter.OrderFilter;

import java.util.List;

/**
 * Service interface for managing order-related operations in the order service.
 * Provides methods for order processing, status updates, filtering, and item modifications within orders.
 */
public interface OrderService {

    /**
     * Creates a new order for a specific user based on the provided request details.
     *
     * @param userId  the unique identifier of the user placing the order
     * @param request the data transfer object containing order details such as items and quantities
     * @return an {@link OrderResponse} containing the created order's details
     */
    OrderResponse createOrder(Long userId, CreateOrderRequest request);

    /**
     * Retrieves a specific order by its unique identifier and user owner identifier.
     *
     * @param userId  the unique identifier of the user requesting the order
     * @param orderId the unique identifier of the order to retrieve
     * @return an {@link OrderResponse} containing the requested order's details
     * @throws RuntimeException if the order is not found or the user lacks authorization
     */
    OrderResponse getOrderById(Long userId, Long orderId);

    /**
     * Retrieves a paginated list of orders matching the specified filtering criteria.
     *
     * @param filter the criteria used to filter orders (e.g., status, price range, dates)
     * @param page   the zero-based page index to retrieve
     * @param size   the maximum number of orders per page
     * @return a {@link PageResponse} containing a list of {@link OrderResponse} objects and pagination info
     */
    PageResponse<OrderResponse> getAllOrders(OrderFilter filter, int page, int size);

    /**
     * Retrieves all orders associated with a specific user.
     *
     * @param userId the unique identifier of the user whose orders are being retrieved
     * @return a {@link List} of {@link OrderResponse} objects belonging to the user
     */
    List<OrderResponse> getOrdersByUserId(Long userId);

    /**
     * Updates the status of an existing order requested by a user or administrator.
     *
     * @param userId  the unique identifier of the user or admin requesting the status update
     * @param orderId the unique identifier of the order to update
     * @param request the data transfer object containing the new order status
     * @return an {@link OrderResponse} reflecting the updated order status
     * @throws RuntimeException if the order with the specified ID is not found
     */
    OrderResponse updateOrderStatusById(Long userId, Long orderId, UpdateOrderStatusRequest request);

    /**
     * Internally updates the status of an order without performing standard user ownership checks.
     * Designed for system-level triggers (e.g., asynchronous payment event processing).
     *
     * @param userId  the unique identifier of the user associated with the order
     * @param orderId the unique identifier of the order to update
     * @param request the data transfer object containing the new status
     */
    void internalUpdateOrderStatus(Long userId, Long orderId, UpdateOrderStatusRequest request);

    /**
     * Updates items or quantities within an existing order.
     *
     * @param userId  the unique identifier of the user updating the order
     * @param orderId the unique identifier of the order to modify
     * @param request the data transfer object containing the updated order item details
     * @return an {@link OrderResponse} containing the updated order state
     * @throws RuntimeException if the order with the specified ID is not found
     */
    OrderResponse updateOrderItemById(Long userId, Long orderId, CreateOrderRequest request);

    /**
     * Deletes (or soft-deletes) an order from the system by its unique identifier.
     *
     * @param userId  the unique identifier of the user requesting the deletion
     * @param orderId the unique identifier of the order to delete
     * @throws RuntimeException if the order with the specified ID is not found
     */
    void deleteOrderById(Long userId, Long orderId);
}