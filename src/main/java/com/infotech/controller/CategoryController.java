package com.infotech.controller;

import java.util.List;

import com.infotech.entity.Category;
import com.infotech.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://192.168.29.45:3000")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        return categoryRepository.save(category);
    }

    @PutMapping("/{id}")
    public Category updateCategory(@PathVariable Long id, @RequestBody Category updatedCategory) {
        return categoryRepository.findById(id).map(category -> {
            category.setVip_id(updatedCategory.getVip_id());
            category.setVip_name(updatedCategory.getVip_name());
            category.setVip_email(updatedCategory.getVip_email());
            category.setVip_designation(updatedCategory.getVip_designation());
            category.setContact_no(updatedCategory.getContact_no());

            // Clear existing items (important for orphanRemoval = true)
            // category.getDataItems().clear();

            // Add updated items with proper category assignment
            // for (DataItem item : updatedCategory.getDataItems()) {
            // item.setCategory(category);
            // category.getDataItems().add(item);
            // }

            return categoryRepository.save(category);
        }).orElseThrow(() -> new RuntimeException("Category not found with id " + id));
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
    }
}
