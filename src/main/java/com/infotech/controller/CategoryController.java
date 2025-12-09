// package com.infotech.controller;
//
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Objects;
// import java.util.Optional;
// import java.util.function.BiConsumer;
//
// import com.infotech.entity.Category;
// import com.infotech.entity.HistoryManagement;
// import com.infotech.repository.CategoryRepository;
// import com.infotech.repository.HistoryManagementRepository;
//
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
//
// import lombok.RequiredArgsConstructor;
//
// @RestController
// @RequestMapping("/api/categories")
// @CrossOrigin(origins = "*")
// @RequiredArgsConstructor
// public class CategoryController {
//
//     private final PasswordEncoder encoder;
//     private final HistoryManagementRepository historyManagementRepository;
//
// private final CategoryRepository categoryRepository;
//
// @GetMapping
// public List<Category> getAllCategories() {
//     return categoryRepository.findAll();
// }
//
//     @PostMapping
//     public Category createCategory(@RequestBody Category category) {
//         category.setPassword(encoder.encode(category.getPassword()));
//         category.setCreatedTime(LocalDateTime.now());
//         return categoryRepository.save(category);
//     }
//
//     @PutMapping("/{id}")
//     public Category updateCategory(@PathVariable Long id, @RequestBody Category updatedCategory) {
//         return categoryRepository.findById(id).map(category -> {
//
//             List<HistoryManagement> historyEntries = new ArrayList<>();
//
//             // helper to record change (handles any type: String, Long, etc.)
//             BiConsumer<String, Object[]> logChange = (fieldName, values) -> {
//                 Object oldVal = values[0];
//                 Object newVal = values[1];
//
//                 // skip if both null or equal
//                 if (Objects.equals(oldVal, newVal)) {
//                     return;
//                 }
//
//                 HistoryManagement history = new HistoryManagement();
//                 history.setTime(LocalDateTime.now());
//                 history.setOperationType("UPDATE");
//                 history.setOperatedBy("Guard"); // later: take from logged-in user
//                 history.setOperatorId(id); // the Category id
//                 history.setEntityName("Category");
//                 history.setFieldName(fieldName);
//
//                 // Convert to String safely (null -> null)
//                 history.setOldValue(oldVal == null ? null : oldVal.toString());
//                 history.setNewValue(newVal == null ? null : newVal.toString());
//
//                 historyEntries.add(history);
//             };
//
//             // name
//             logChange.accept("name", new Object[] {
//                     category.getName(),
//                     updatedCategory.getName()
//             });
//             category.setName(updatedCategory.getName());
//
//             // email
//             logChange.accept("email", new Object[] {
//                     category.getEmail(),
//                     updatedCategory.getEmail()
//             });
//             category.setEmail(updatedCategory.getEmail());
//
//             // status
//             logChange.accept("status", new Object[] {
//                     category.getStatus(),
//                     updatedCategory.getStatus()
//             });
//             category.setStatus(updatedCategory.getStatus());
//
//             // designation
//             logChange.accept("designation", new Object[] {
//                     category.getDesignation(),
//                     updatedCategory.getDesignation()
//             });
//             category.setDesignation(updatedCategory.getDesignation());
//
//             // contactno (even if it's Long, now it's fine)
//             logChange.accept("contactno", new Object[] {
//                     category.getContactno(),
//                     updatedCategory.getContactno()
//             });
//             category.setContactno(updatedCategory.getContactno());
//
//             // username
//             logChange.accept("username", new Object[] {
//                     category.getUsername(),
//                     updatedCategory.getUsername()
//             });
//             category.setUsername(updatedCategory.getUsername());
//
//             // password (only if user sent a new one & it's actually different)
//             String newRawPassword = updatedCategory.getPassword();
//             if (newRawPassword != null && !newRawPassword.isBlank()) {
//
//                 // Check if the new raw password is different from existing encoded one
//                 boolean sameAsOld = encoder.matches(newRawPassword, category.getPassword());
//
//                 if (!sameAsOld) {
//                     // Log only that password was updated (never store real values)
//                     HistoryManagement history = new HistoryManagement();
//                     history.setTime(LocalDateTime.now());
//                     history.setOperationType("UPDATE");
//                     history.setOperatedBy("Guard");
//                     history.setOperatorId(id);
//                     history.setEntityName("Category");
//                     history.setFieldName("password");
//                     history.setOldValue(null); // or "MASKED"
//                     history.setNewValue("UPDATED"); // means "password changed"
//                     historyEntries.add(history);
//
//                     // Store new encoded password
//                     category.setPassword(encoder.encode(newRawPassword));
//                 }
//             }
//
//             // Save category
//             Category saved = categoryRepository.save(category);
//
//             // Save history for changed fields
//             if (!historyEntries.isEmpty()) {
//                 historyManagementRepository.saveAll(historyEntries);
//             }
//
//             return saved;
//
//         }).orElseThrow(() -> new RuntimeException("Category not found with id " + id));
//     }
//
//     @DeleteMapping("/{id}")
//     public void deleteCategory(@PathVariable Long id) {
//         Category cat = categoryRepository.findById(id).map(category -> {
//             category.setStatus("Deleted");
//             return categoryRepository.save(category);
//         }).orElseThrow(() -> new RuntimeException("Category not found with id " + id));
//         System.out.println("data deleted Successfully");
//         HistoryManagement history = new HistoryManagement();
//
//         // history.setTime(LocalDateTime.now());
//         // history.setOprationType("update");
//         // history.setOperatedBy("Guard");
//         // history.setOperatorId(id);
//         //
//         // historyManagementRepository.save(history);
//     }
//

//
// }

package com.infotech.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.infotech.dto.VipProfileResponse;
import com.infotech.entity.Category;
import com.infotech.repository.CategoryRepository;
import com.infotech.service.CategoryService;

import org.springframework.http.ResponseEntity;
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
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    private final CategoryRepository categoryRepository;

    // In real app, get from
    // SecurityContextHolder.getContext().getAuthentication().getName()
    private String getCurrentOperator() {
        return "Guard"; // placeholder
    }

    @GetMapping
    public List<Category> getAllCategories() {
        // System.out.println("called data");
        return categoryRepository.findAll();
    }

    // CREATE or RESTORE
    @PostMapping("/register/{role}")
    public ResponseEntity<Category> createCategory(@RequestBody Category newCategory, @PathVariable String role) {
        System.out.println("category called");
        newCategory.setCreatedTime(LocalDateTime.now());
        Category saved = categoryService.createOrRestoreCategory(newCategory, role);
        return ResponseEntity.ok(saved);
    }

    // UPDATE
    @PutMapping("/{id}/{role}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @PathVariable String role,
            @RequestBody Category updatedCategory) {
        // updatedCategory.setCreatedTime(LocalDateTime.now());
        Category updated = categoryService.updateCategory(id, updatedCategory, role);
        return ResponseEntity.ok(updated);
    }

    // SOFT DELETE
    @DeleteMapping("/{id}/{operator}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, @PathVariable String operator) {
        categoryService.softDeleteCategory(id, operator);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public VipProfileResponse getAdmin(@RequestParam String username) {
        Category admindata = categoryRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vip data not found"));
        VipProfileResponse res = new VipProfileResponse();
        res.setId(admindata.getId());
        res.setName(admindata.getName());
        res.setEmail(admindata.getEmail());
        res.setUsername(admindata.getUsername());
        res.setStatus(admindata.getStatus());
        res.setContactno(admindata.getContactno());
        System.out.println(admindata.getPic() + "pic id value");
        if (admindata.getPic() != null) {
            res.setUrl(admindata.getPic().getUrl());
            System.out.println(admindata.getPic().getUrl() + "pic Url value ");
        }
        return res;
    }
}
