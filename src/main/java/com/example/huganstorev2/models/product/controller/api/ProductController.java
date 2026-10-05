package com.example.huganstorev2.models.product.controller.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.huganstorev2.models.product.controller.Dtos.ProductRequest;
import com.example.huganstorev2.models.product.controller.Dtos.ProductPopularityResponse;
import com.example.huganstorev2.models.product.controller.Dtos.ProductResponse;
import com.example.huganstorev2.models.product.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController 
@RequestMapping ("/api/v1/products")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @GetMapping 
    @Operation (summary = "Lấy danh sách sản phẩm")
    public ResponseEntity<List<ProductResponse>> getAllProduct(){
        return ResponseEntity.ok(productService.getAllProduct());
    }

    @GetMapping("/popularity")
    @Operation(summary = "Thống kê số lượng đang trong giỏ và đã đặt theo sản phẩm")
    public ResponseEntity<List<ProductPopularityResponse>> getProductPopularity(){
        return ResponseEntity.ok(productService.getProductPopularity());
    }

    @GetMapping("/{id}")
    @Operation (summary = "Lấy sản phẩm theo id")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @Operation (summary = "Tạo sản phẩm - Chỉ ADMIN")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request){
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation (summary = "Cập nhật sản phẩm - Chỉ ADMIN")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(@RequestBody @Valid  ProductRequest request, @PathVariable Long id){
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation (summary = "Xóa sản phẩm - Chỉ ADMIN")
    @PreAuthorize ("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
