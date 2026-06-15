package com.innowise.order_service.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.order_service.BaseIntegrationTest;
import com.innowise.order_service.dto.request.CreateOrderRequest;
import com.innowise.order_service.dto.request.OrderItemRequest;
import com.innowise.order_service.dto.request.UpdateOrderStatusRequest;
import com.innowise.order_service.jpa.entity.Item;
import com.innowise.order_service.jpa.entity.Order;
import com.innowise.order_service.jpa.enums.OrderStatus;
import com.innowise.order_service.jpa.repository.ItemRepository;
import com.innowise.order_service.jpa.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;

    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();

        userToken = "Bearer " + generateTestToken("42", "USER");
        adminToken = "Bearer " + generateTestToken("100", "ADMIN");
    }

    private String generateTestToken(String subject, String role) {
        try {
            return io.jsonwebtoken.Jwts.builder()
                    .subject(subject)
                    .claim("role", role)
                    .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                            java.security.MessageDigest.getInstance("SHA-256")
                                    .digest(secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                    ))
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("POST /api/orders - Success")
    void createOrder_Success() throws Exception {
        Item item = itemRepository.save(new Item(null, "Mechanical Keyboard", BigDecimal.valueOf(100.00)));

        CreateOrderRequest request = new CreateOrderRequest();
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setItemId(item.getId());
        itemReq.setQuantity(2);
        request.setOrderItems(List.of(itemReq));

        wireMockServer.stubFor(WireMock.get(urlEqualTo("/store/users/me"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 42, \"name\": \"John\", \"email\": \"john@example.com\"}")));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.totalPrice").value(200.00))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.user.name").value("John"));
    }

    @Test
    @DisplayName("POST /api/orders - User Service Unavailable (Circuit Breaker / Fallback Test)")
    void createOrder_UserServiceUnavailable_ThrowsCustomException() throws Exception {
        Item item = itemRepository.save(new Item(null, "Mouse", BigDecimal.TEN));
        CreateOrderRequest request = new CreateOrderRequest();
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setItemId(item.getId());
        itemReq.setQuantity(1);
        request.setOrderItems(List.of(itemReq));

        wireMockServer.stubFor(WireMock.get(urlEqualTo("/store/users/me"))
                .willReturn(aResponse().withStatus(503)));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value("USER_SERVICE_UNAVAILABLE"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Access Denied for Foreign Order")
    void getOrderById_ForeignOrder_ReturnsForbidden() throws Exception {
        Order order = new Order();
        order.setUserId(999L);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.ZERO);
        order = orderRepository.save(order);

        mockMvc.perform(get("/api/orders/" + order.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/orders/{id}/status - Success as ADMIN")
    void updateOrderStatus_AsAdmin_Success() throws Exception {
        Order order = new Order();
        order.setUserId(42L);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.ZERO);
        order = orderRepository.save(order);

        UpdateOrderStatusRequest statusRequest = new UpdateOrderStatusRequest();
        statusRequest.setStatus("PROCESSING");

        wireMockServer.stubFor(WireMock.get(urlEqualTo("/store/users/42"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 42, \"name\": \"John\"}")));

        mockMvc.perform(patch("/api/orders/" + order.getId() + "/status")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Success as Owner")
    void getOrderById_AsOwner_Success() throws Exception {
        Order order = new Order();
        order.setUserId(42L);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.ZERO);
        order = orderRepository.save(order);

        wireMockServer.stubFor(WireMock.get(urlEqualTo("/store/users/me"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 42, \"name\": \"John\"}")));

        mockMvc.perform(get("/api/orders/" + order.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(order.getId()));
    }

    @Test
    @DisplayName("GET /api/orders/me - Success")
    void getUserOrders_Success() throws Exception {
        Order order = new Order();
        order.setUserId(42L);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.ZERO);
        orderRepository.save(order);

        wireMockServer.stubFor(WireMock.get(urlEqualTo("/store/users/me"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 42, \"name\": \"John\"}")));

        mockMvc.perform(get("/api/orders/me")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/orders - Success as ADMIN")
    void getAllOrders_AsAdmin_Success() throws Exception {
        String criteriaJson = """
                {
                    "status": "CREATED",
                    "page": 0,
                    "size": 10
                }
                """;

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criteriaJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("PATCH /api/orders/{id} - Update Order Items Success")
    void updateOrderItem_Success() throws Exception {
        Order order = new Order();
        order.setUserId(42L);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.ZERO);
        order = orderRepository.save(order);

        Item item = itemRepository.save(new Item(null, "Updated Item", BigDecimal.TEN));

        CreateOrderRequest updateRequest = new CreateOrderRequest();
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setItemId(item.getId());
        itemReq.setQuantity(3);
        updateRequest.setOrderItems(List.of(itemReq));

        wireMockServer.stubFor(WireMock.get(urlEqualTo("/store/users/me"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 42, \"name\": \"John\"}")));

        mockMvc.perform(patch("/api/orders/" + order.getId())
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(30.00));
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} - Success")
    void deleteOrder_Success() throws Exception {
        Order order = new Order();
        order.setUserId(42L);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.ZERO);
        order = orderRepository.save(order);

        mockMvc.perform(delete("/api/orders/" + order.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isNoContent());
    }
}