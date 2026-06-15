package com.innowise.order_service.controller;

import com.innowise.order_service.BaseIntegrationTest;
import com.innowise.order_service.dto.request.CreateItemRequest;
import com.innowise.order_service.dto.request.UpdateItemRequest;
import com.innowise.order_service.jpa.entity.Item;
import com.innowise.order_service.jpa.repository.ItemRepository;
import com.innowise.order_service.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ItemControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    private String adminToken;
    private String userToken;

    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();

        adminToken = "Bearer " + generateTestToken("1", "ADMIN");
        userToken = "Bearer " + generateTestToken("2", "USER");
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
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("POST /api/items - Success as ADMIN")
    void createItem_AsAdmin_Success() throws Exception {
        CreateItemRequest request = new CreateItemRequest();
        request.setName("Gaming Laptop");
        request.setPrice(BigDecimal.valueOf(1200.50));

        mockMvc.perform(post("/api/items")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemId").exists())
                .andExpect(jsonPath("$.name").value("Gaming Laptop"))
                .andExpect(jsonPath("$.price").value(1200.50));
    }

    @Test
    @DisplayName("POST /api/items - Forbidden as USER")
    void createItem_AsUser_Forbidden() throws Exception {
        CreateItemRequest request = new CreateItemRequest();
        request.setName("Forbidden Item");
        request.setPrice(BigDecimal.TEN);

        mockMvc.perform(post("/api/items")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/items/{id} - Success")
    void getItemById_Success() throws Exception {
        Item item = itemRepository.save(new Item(null, "Smartphone", BigDecimal.valueOf(699.99)));

        mockMvc.perform(get("/api/items/" + item.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Smartphone"))
                .andExpect(jsonPath("$.price").value(699.99));
    }

    @Test
    @DisplayName("GET /api/items/{id} - NotFound")
    void getItemById_NotFound() throws Exception {
        mockMvc.perform(get("/api/items/9999")
                        .header("Authorization", userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ITEM_NOT_FOUND"));
    }

    @Test
    @DisplayName("PUT /api/items/{id} - Success")
    void updateItem_Success() throws Exception {
        Item item = itemRepository.save(new Item(null, "Old Name", BigDecimal.ONE));
        UpdateItemRequest updateRequest = new UpdateItemRequest();
        updateRequest.setName("New Name");
        updateRequest.setPrice(BigDecimal.TEN);

        mockMvc.perform(put("/api/items/" + item.getId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.price").value(10.00));
    }

    @Test
    @DisplayName("DELETE /api/items/{id} - Success")
    void deleteItem_Success() throws Exception {
        Item item = itemRepository.save(new Item(null, "To Delete", BigDecimal.ONE));

        mockMvc.perform(delete("/api/items/" + item.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isNoContent());

        assertFalse(itemRepository.findById(item.getId()).isPresent());
    }

    @Test
    @DisplayName("GET /api/items - Success with search criteria")
    void getAllItems_Success() throws Exception {
        itemRepository.save(new Item(null, "Gaming Chair", BigDecimal.valueOf(250.00)));

        String criteriaJson = """
                {
                    "name": "Gaming Chair",
                    "startPrice": 200.00,
                    "finishPrice":300.00,
                    "page": 0,
                    "size": 10
                }
                """;

        mockMvc.perform(get("/api/items")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criteriaJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").exists());
    }

}