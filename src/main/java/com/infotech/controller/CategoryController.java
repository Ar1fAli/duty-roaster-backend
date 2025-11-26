package com.infotech.controller;

import java.util.List;
import java.util.Optional;

import com.infotech.entity.Category;
import com.infotech.repository.CategoryRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CategoryController {

    private final PasswordEncoder encoder;

    private final CategoryRepository categoryRepository;

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        category.setPassword(encoder.encode(category.getPassword()));
        return categoryRepository.save(category);
    }

    @PutMapping("/{id}")
    public Category updateCategory(@PathVariable Long id, @RequestBody Category updatedCategory) {
        return categoryRepository.findById(id).map(category -> {
            category.setId(updatedCategory.getId());
            category.setName(updatedCategory.getName());
            category.setEmail(updatedCategory.getEmail());
            category.setStatus(updatedCategory.getStatus());
            category.setDesignation(updatedCategory.getDesignation());
            category.setContactno(updatedCategory.getContactno());
            category.setUsername(updatedCategory.getUsername());
            category.setPassword(encoder.encode(updatedCategory.getPassword()));

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

    @GetMapping("/profile")
    public Optional<Category> getAdmin(@RequestParam String username) {
        Optional<Category> admindata = categoryRepository.findByUsername(username);
        return admindata;
    }

}
