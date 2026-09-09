package com.example.huganstorev2.models.category.service;

import org.springframework.stereotype.Service;

import com.example.huganstorev2.models.category.repository.CategoryRepository;
import com.example.huganstorev2.models.category.entity.Category;

import java.text.Normalizer;
import java.util.List;

@Service 
public class CategoryService {
    private final CategoryRepository categoryRepository;
    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }
    // Lấy danh sách danh mục
    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }
    // Lấy theo id
    public Category getCategoryById(Long id){
        return categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
    }
    // Tạo danh mục 
    public String generateSlug(String name){
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);

        return normalized.replaceAll("\\p{M}", "")
        .toLowerCase().trim()
        .replaceAll("\\s+", "-");
    }
    public Category createCategory(Category category){
        String slug = generateSlug(category.getName());
        category.setSlug(slug);
        return categoryRepository.save(category);
    }
    // Cập nhật
    public Category updateCategory(Long id, Category category){
        Category existingCategory = categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
        
        existingCategory.setName(category.getName());
        existingCategory.setSlug(category.getSlug());

        return categoryRepository.save(existingCategory);
    }
    // Xóa
    public void deleteCategory(Long id){
        Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
        categoryRepository.delete(category);
    }
}
