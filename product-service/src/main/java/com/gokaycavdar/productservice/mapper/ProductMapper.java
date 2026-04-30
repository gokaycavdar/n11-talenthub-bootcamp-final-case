package com.gokaycavdar.productservice.mapper;

import org.mapstruct.Mapper;

import com.gokaycavdar.productservice.dto.ProductResponse;
import com.gokaycavdar.productservice.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toProductResponse(Product product);
}
