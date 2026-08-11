package com.n2n.booking.service;

import com.n2n.booking.dto.ProductDTO;
import com.n2n.booking.entity.Product;
import com.n2n.booking.exception.ResourceNotFoundException;
import com.n2n.booking.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductDTO> getAllProducts(String category) {
        List<Product> products;
        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("all")) {
            products = productRepository.findByCategoryIgnoreCase(category);
        } else {
            products = productRepository.findAll();
        }
        return products.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToDTO(product);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = Product.builder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .category(productDTO.getCategory())
                .capacity(productDTO.getCapacity() != null ? productDTO.getCapacity() : 1)
                .imageUrl(productDTO.getImageUrl())
                .status(productDTO.getStatus() != null ? productDTO.getStatus() : "AVAILABLE")
                .build();

        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setCategory(productDTO.getCategory());
        if (productDTO.getCapacity() != null) {
            product.setCapacity(productDTO.getCapacity());
        }
        if (productDTO.getImageUrl() != null) {
            product.setImageUrl(productDTO.getImageUrl());
        }
        if (productDTO.getStatus() != null) {
            product.setStatus(productDTO.getStatus());
        }

        Product updatedProduct = productRepository.save(product);
        return mapToDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    public ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .capacity(product.getCapacity())
                .imageUrl(product.getImageUrl())
                .status(product.getStatus())
                .build();
    }
}
