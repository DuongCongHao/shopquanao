package com.example.huganstorev2.models.category.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.huganstorev2.models.category.entity.Category;
import com.example.huganstorev2.models.category.service.CategoryService;
import com.example.huganstorev2.models.category.service.Dtos.CategoryRequest;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;

@RestController 
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }

    @GetMapping 
    @Operation (summary = "Lấy danh sách danh mục")
    public ResponseEntity<?> getAllCategories(){
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}") 
    @Operation (summary = "Lấy danh mục theo id")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id){
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation (summary = "Tạo danh mục - dành cho ADMIN")
    public ResponseEntity<?> createCategory(@RequestBody CategoryRequest request){
        Category category = new Category();
        category.setName(request.getName());

        Category saveCategory = categoryService.createCategory(category);

        return ResponseEntity.status(HttpStatus.CREATED).body(saveCategory);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation (summary = "Xóa danh mục - dành cho ADMIN")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
