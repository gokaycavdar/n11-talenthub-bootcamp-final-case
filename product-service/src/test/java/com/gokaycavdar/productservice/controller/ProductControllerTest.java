package com.gokaycavdar.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gokaycavdar.productservice.dto.CreateProductRequest;
import com.gokaycavdar.productservice.dto.ProductPageResponse;
import com.gokaycavdar.productservice.dto.ProductResponse;
import com.gokaycavdar.productservice.dto.UpdateProductRequest;
import com.gokaycavdar.productservice.exception.GlobalExceptionHandler;
import com.gokaycavdar.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void getProducts_shouldReturnPagedProducts() throws Exception {
        ProductResponse productResponse = new ProductResponse(
                1L,
                "iPhone 15",
                "Telefon",
                "Telefon",
                BigDecimal.valueOf(59999.00),
                10,
                "/images/products/iphone-15.jpg",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        ProductPageResponse pageResponse = new ProductPageResponse(
                List.of(productResponse),
                0,
                10,
                1,
                1,
                true
        );

        when(productService.getProducts(0, 10, "id")).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("iPhone 15"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getProductById_shouldReturnProduct() throws Exception {
        ProductResponse productResponse = new ProductResponse(
                1L,
                "iPhone 15",
                "Telefon",
                "Telefon",
                BigDecimal.valueOf(59999.00),
                10,
                "/images/products/iphone-15.jpg",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(productService.getProductById(1L)).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("iPhone 15"));
    }

    @Test
    void createProduct_shouldReturnCreatedProduct() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "Dell XPS 13",
                "Laptop",
                "Bilgisayar",
                BigDecimal.valueOf(45999.00),
                7,
                "/images/products/dell-xps-13.jpg"
        );

        ProductResponse response = new ProductResponse(
                1L,
                request.name(),
                request.description(),
                request.category(),
                request.price(),
                request.stock(),
                request.imageUrl(),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dell XPS 13"));
    }

    @Test
    void updateProduct_shouldReturnUpdatedProduct() throws Exception {
        UpdateProductRequest request = new UpdateProductRequest(
                "Updated Product",
                "Updated Desc",
                "Telefon",
                BigDecimal.valueOf(999.99),
                5,
                "/images/products/updated.jpg",
                true
        );

        ProductResponse response = new ProductResponse(
                1L,
                request.name(),
                request.description(),
                request.category(),
                request.price(),
                request.stock(),
                request.imageUrl(),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(productService.updateProduct(eq(1L), any(UpdateProductRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"));
    }

    @Test
    void deleteProduct_shouldReturnNoContent() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());
    }
}
