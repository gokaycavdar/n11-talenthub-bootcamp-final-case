package com.gokaycavdar.productservice.service;

import com.gokaycavdar.productservice.dto.CreateProductRequest;
import com.gokaycavdar.productservice.dto.ProductPageResponse;
import com.gokaycavdar.productservice.dto.ProductResponse;
import com.gokaycavdar.productservice.dto.UpdateProductRequest;
import com.gokaycavdar.productservice.entity.Product;
import com.gokaycavdar.productservice.exception.ResourceNotFoundException;
import com.gokaycavdar.productservice.mapper.ProductMapper;
import com.gokaycavdar.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProducts_shouldReturnPagedResponse() {
        Product product = Product.builder()
                .id(1L)
                .name("iPhone 15")
                .description("Telefon")
                .category("Telefon")
                .price(BigDecimal.valueOf(59999.00))
                .stock(10)
                .imageUrl("/images/products/iphone-15.jpg")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ProductResponse productResponse = new ProductResponse(
                1L,
                "iPhone 15",
                "Telefon",
                "Telefon",
                BigDecimal.valueOf(59999.00),
                10,
                "/images/products/iphone-15.jpg",
                true,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );

        when(productRepository.findByActiveTrue(any()))
                .thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1));
        when(productMapper.toProductResponse(product)).thenReturn(productResponse);

        ProductPageResponse response = productService.getProducts(0, 10, "id");

        assertEquals(1, response.content().size());
        assertEquals("iPhone 15", response.content().get(0).name());
        assertEquals(0, response.page());
        assertEquals(10, response.size());
        assertEquals(1, response.totalElements());
    }

    @Test
    void getProductById_shouldReturnProductResponse() {
        Product product = Product.builder()
                .id(1L)
                .name("iPhone 15")
                .description("Telefon")
                .category("Telefon")
                .price(BigDecimal.valueOf(59999.00))
                .stock(10)
                .imageUrl("/images/products/iphone-15.jpg")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ProductResponse productResponse = new ProductResponse(
                1L,
                "iPhone 15",
                "Telefon",
                "Telefon",
                BigDecimal.valueOf(59999.00),
                10,
                "/images/products/iphone-15.jpg",
                true,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );

        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));
        when(productMapper.toProductResponse(product)).thenReturn(productResponse);

        ProductResponse response = productService.getProductById(1L);

        assertEquals(1L, response.id());
        assertEquals("iPhone 15", response.name());
    }

    @Test
    void getProductById_shouldThrow_whenProductNotFound() {
        when(productRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(999L)
        );

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void createProduct_shouldPersistAndReturnResponse() {
        CreateProductRequest request = new CreateProductRequest(
                "Dell XPS 13",
                "Laptop",
                "Bilgisayar",
                BigDecimal.valueOf(45999.00),
                7,
                "/images/products/dell-xps-13.jpg"
        );

        Product savedProduct = Product.builder()
                .id(1L)
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .price(request.price())
                .stock(request.stock())
                .imageUrl(request.imageUrl())
                .active(true)
                .build();

        ProductResponse productResponse = new ProductResponse(
                1L,
                request.name(),
                request.description(),
                request.category(),
                request.price(),
                request.stock(),
                request.imageUrl(),
                true,
                null,
                null
        );

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productMapper.toProductResponse(savedProduct)).thenReturn(productResponse);

        ProductResponse response = productService.createProduct(request);

        assertEquals("Dell XPS 13", response.name());
        assertEquals(BigDecimal.valueOf(45999.00), response.price());
        assertTrue(response.active());
    }

    @Test
    void updateProduct_shouldUpdateExistingProduct() {
        Product product = Product.builder()
                .id(1L)
                .name("Old Name")
                .description("Old Desc")
                .category("Old Category")
                .price(BigDecimal.valueOf(100))
                .stock(1)
                .imageUrl("/old.jpg")
                .active(true)
                .build();

        UpdateProductRequest request = new UpdateProductRequest(
                "New Name",
                "New Desc",
                "New Category",
                BigDecimal.valueOf(200),
                5,
                "/new.jpg",
                false
        );

        Product updatedProduct = Product.builder()
                .id(1L)
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .price(request.price())
                .stock(request.stock())
                .imageUrl(request.imageUrl())
                .active(request.active())
                .build();

        ProductResponse productResponse = new ProductResponse(
                1L,
                request.name(),
                request.description(),
                request.category(),
                request.price(),
                request.stock(),
                request.imageUrl(),
                false,
                null,
                null
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(updatedProduct);
        when(productMapper.toProductResponse(updatedProduct)).thenReturn(productResponse);

        ProductResponse response = productService.updateProduct(1L, request);

        assertEquals("New Name", response.name());
        assertFalse(response.active());
    }

    @Test
    void deleteProduct_shouldSoftDeleteProduct() {
        Product product = Product.builder()
                .id(1L)
                .active(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        assertFalse(product.getActive());
        verify(productRepository).save(product);
    }
}
