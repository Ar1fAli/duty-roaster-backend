// package com.infotech.service;
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
// import org.springframework.stereotype.Service;
//
// import lombok.RequiredArgsConstructor;
//
// @Service
// @RequiredArgsConstructor
// public class CategoryService {
//
//     private static final String STATUS_ACTIVE = "Inactive";
//     private static final String STATUS_DELETED = "Deleted";
//
//     private final CategoryRepository categoryRepository;
//     private final HistoryManagementRepository historyManagementRepository;
//     private final PasswordEncoder encoder;
//
//     // ───────────────────────────── CREATE / RESTORE ─────────────────────────────
//
//     public Category createOrRestoreCategory(Category newCategory, String operatedBy) {
//
//         if (newCategory.getUsername() == null || newCategory.getUsername().isBlank()) {
//             throw new RuntimeException("Username is required");
//         }
//
//         if (newCategory.getPassword() == null || newCategory.getPassword().isBlank()) {
//             throw new RuntimeException("Password is required");
//         }
//
//         // Find a soft-deleted match by username/email/contactno
//         Optional<Category> softDeletedOpt = findSoftDeletedMatch(newCategory);
//
//         if (softDeletedOpt.isPresent()) {
//             Category deletedCat = softDeletedOpt.get();
//             String oldStatus = deletedCat.getStatus();
//
//             deletedCat.setStatus(STATUS_ACTIVE);
//             deletedCat.setName(newCategory.getName());
//             deletedCat.setEmail(newCategory.getEmail());
//             deletedCat.setDesignation(newCategory.getDesignation());
//             deletedCat.setContactno(newCategory.getContactno());
//             deletedCat.setUsername(newCategory.getUsername());
//             deletedCat.setPassword(encoder.encode(newCategory.getPassword()));
//
//             Category restored = categoryRepository.save(deletedCat);
//
//             // Log RESTORE
//             logHistory("RESTORE", "Category", restored.getId(), operatedBy,
//                     "status", oldStatus, STATUS_ACTIVE);
//
//             return restored;
//         }
//
//         // No soft-deleted match → create new
//         newCategory.setStatus(STATUS_ACTIVE);
//         newCategory.setPassword(encoder.encode(newCategory.getPassword()));
//
//         Category saved = categoryRepository.save(newCategory);
//
//         // Log CREATE
//         logHistory("CREATE", "Category", saved.getId(), operatedBy,
//                 "status", null, STATUS_ACTIVE);
//
//         return saved;
//     }
//
//     /**
//      * Find a soft-deleted Category that matches by username OR email OR contactno.
//      */
//     private Optional<Category> findSoftDeletedMatch(Category newCategory) {
//         List<Category> candidates = new ArrayList<>();
//
//         if (newCategory.getUsername() != null && !newCategory.getUsername().isBlank()) {
//             categoryRepository
//                     .findByUsernameAndStatus(newCategory.getUsername(), STATUS_DELETED)
//                     .ifPresent(candidates::add);
//         }
//
//         if (newCategory.getEmail() != null && !newCategory.getEmail().isBlank()) {
//             categoryRepository
//                     .findByEmailAndStatus(newCategory.getEmail(), STATUS_DELETED)
//                     .ifPresent(candidates::add);
//         }
//
//         if (newCategory.getContactno() != null) { // contactno is Long
//             categoryRepository
//                     .findByContactnoAndStatus(newCategory.getContactno(), STATUS_DELETED)
//                     .ifPresent(candidates::add);
//         }
//
//         if (candidates.isEmpty()) {
//             return Optional.empty();
//         }
//
//         // If multiple soft-deleted records match, you can change this logic
//         return Optional.of(candidates.get(0));
//     }
//
//     // ───────────────────────────── UPDATE ─────────────────────────────
//
//     public Category updateCategory(Long id, Category updatedCategory, String operatedBy) {
//         return categoryRepository.findById(id).map(category -> {
//
//             List<HistoryManagement> historyEntries = new ArrayList<>();
//
//             // helper: log change for any type field
//             BiConsumer<String, Object[]> logChange = (fieldName, values) -> {
//                 Object oldVal = values[0];
//                 Object newVal = values[1];
//
//                 if (Objects.equals(oldVal, newVal)) {
//                     return;
//                 }
//
//                 HistoryManagement history = new HistoryManagement();
//                 history.setTime(LocalDateTime.now());
//                 history.setOperationType("UPDATE");
//                 history.setOperatedBy(operatedBy);
//                 history.setOperatorId(id);
//                 history.setEntityName("Category");
//                 history.setFieldName(fieldName);
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
//             // contactno (Long)
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
//                 boolean sameAsOld = encoder.matches(newRawPassword, category.getPassword());
//
//                 if (!sameAsOld) {
//                     // history: only mark that password updated
//                     logHistory("UPDATE", "Category", id, operatedBy,
//                             "password", null, "UPDATED");
//
//                     category.setPassword(encoder.encode(newRawPassword));
//                 }
//             }
//
//             Category saved = categoryRepository.save(category);
//
//             if (!historyEntries.isEmpty()) {
//                 historyManagementRepository.saveAll(historyEntries);
//             }
//
//             return saved;
//
//         }).orElseThrow(() -> new RuntimeException("Category not found with id " + id));
//     }
//
//     // ───────────────────────────── DELETE (SOFT) ─────────────────────────────
//
//     public void softDeleteCategory(Long id, String operatedBy) {
//         Category category = categoryRepository.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Category not found with id " + id));
//
//         String oldStatus = category.getStatus();
//
//         category.setStatus(STATUS_DELETED);
//         categoryRepository.save(category);
//
//         // log delete
//         logHistory("DELETE", "Category", id, operatedBy,
//                 "status", oldStatus, STATUS_DELETED);
//     }
//
//     // ───────────────────────────── HISTORY HELPER ─────────────────────────────
//
//     private void logHistory(String operationType,
//             String entityName,
//             Long operatorId,
//             String operatedBy,
//             String fieldName,
//             String oldValue,
//             String newValue) {
//
//         HistoryManagement history = new HistoryManagement();
//         history.setTime(LocalDateTime.now());
//         history.setOperationType(operationType);
//         history.setOperatedBy(operatedBy);
//         history.setOperatorId(operatorId);
//         history.setEntityName(entityName);
//         history.setFieldName(fieldName);
//         history.setOldValue(oldValue);
//         history.setNewValue(newValue);
//
//         historyManagementRepository.save(history);
//     }
// }
//
//
//
//
//
//
//
//

// package com.infotech.service;
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
// import com.infotech.exception.BadRequestException;
// import com.infotech.repository.CategoryRepository;
// import com.infotech.repository.HistoryManagementRepository;
//
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Service;
//
// import lombok.RequiredArgsConstructor;
//
// @Service
// @RequiredArgsConstructor
// public class CategoryService {
//
//     private static final String STATUS_ACTIVE = "Inactive";
//     private static final String STATUS_DELETED = "Deleted";
//
//     private final CategoryRepository categoryRepository;
//     private final HistoryManagementRepository historyManagementRepository;
//     private final PasswordEncoder encoder;
//
//     // ───────────────────────────── UNIQUE FIELD VALIDATION
//     // ─────────────────────────────
//
//     /**
//      * Validates that username, email and contactno are unique among all
//      * NON-DELETED categories.
//      *
//      * @param dto      category data (create/update)
//      * @param ignoreId category id to ignore (for update), or null for create.
//      * @throws BadRequestException if any field already exists.
//      */
//     private void validateUniqueFields(Category dto, Long ignoreId) {
//
//         // username
//         if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
//             categoryRepository.findByUsernameAndStatusNot(dto.getUsername(), STATUS_DELETED)
//                     .filter(c -> !Objects.equals(c.getId(), ignoreId))
//                     .ifPresent(c -> {
//                         throw new BadRequestException("Username already exists");
//                     });
//         }
//
//         // email
//         if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
//             categoryRepository.findByEmailAndStatusNot(dto.getEmail(), STATUS_DELETED)
//                     .filter(c -> !Objects.equals(c.getId(), ignoreId))
//                     .ifPresent(c -> {
//                         throw new BadRequestException("Email already exists");
//                     });
//         }
//
//         // contactno
//         if (dto.getContactno() != null) {
//             categoryRepository.findByContactnoAndStatusNot(dto.getContactno(), STATUS_DELETED)
//                     .filter(c -> !Objects.equals(c.getId(), ignoreId))
//                     .ifPresent(c -> {
//                         throw new BadRequestException("Contact number already exists");
//                     });
//         }
//     }
//
//     // ───────────────────────────── CREATE / RESTORE ─────────────────────────────
//
//     public Category createOrRestoreCategory(Category newCategory, String operatedBy) {
//
//         if (newCategory.getUsername() == null || newCategory.getUsername().isBlank()) {
//             throw new RuntimeException("Username is required");
//         }
//
//         if (newCategory.getPassword() == null || newCategory.getPassword().isBlank()) {
//             throw new RuntimeException("Password is required");
//         }
//
//         // uniqueness check for create (ignoreId = null)
//         validateUniqueFields(newCategory, null);
//
//         // Find a soft-deleted match by username/email/contactno
//         Optional<Category> softDeletedOpt = findSoftDeletedMatch(newCategory);
//
//         if (softDeletedOpt.isPresent()) {
//             Category deletedCat = softDeletedOpt.get();
//             String oldStatus = deletedCat.getStatus();
//
//             deletedCat.setStatus(STATUS_ACTIVE);
//             deletedCat.setName(newCategory.getName());
//             deletedCat.setEmail(newCategory.getEmail());
//             deletedCat.setDesignation(newCategory.getDesignation());
//             deletedCat.setContactno(newCategory.getContactno());
//             deletedCat.setUsername(newCategory.getUsername());
//             deletedCat.setPassword(encoder.encode(newCategory.getPassword()));
//
//             Category restored = categoryRepository.save(deletedCat);
//
//             // Log RESTORE
//             logHistory("RESTORE", "VIP", restored.getId(), operatedBy,
//                     "status", oldStatus, STATUS_ACTIVE);
//
//             return restored;
//         }
//
//         // No soft-deleted match → create new
//         newCategory.setStatus(STATUS_ACTIVE);
//         newCategory.setPassword(encoder.encode(newCategory.getPassword()));
//
//         Category saved = categoryRepository.save(newCategory);
//         System.out.println(saved + "  category created ");
//
//         // Log CREATE
//         logHistory("CREATE", "VIP", saved.getId(), operatedBy,
//                 "status", null, STATUS_ACTIVE);
//
//         return saved;
//     }
//
//     /**
//      * Find a soft-deleted Category that matches by username OR email OR contactno.
//      */
//     private Optional<Category> findSoftDeletedMatch(Category newCategory) {
//         List<Category> candidates = new ArrayList<>();
//
//         if (newCategory.getUsername() != null && !newCategory.getUsername().isBlank()) {
//             categoryRepository
//                     .findByUsernameAndStatus(newCategory.getUsername(), STATUS_DELETED)
//                     .ifPresent(candidates::add);
//         }
//
//         if (newCategory.getEmail() != null && !newCategory.getEmail().isBlank()) {
//             categoryRepository
//                     .findByEmailAndStatus(newCategory.getEmail(), STATUS_DELETED)
//                     .ifPresent(candidates::add);
//         }
//
//         if (newCategory.getContactno() != null) { // contactno is Long
//             categoryRepository
//                     .findByContactnoAndStatus(newCategory.getContactno(), STATUS_DELETED)
//                     .ifPresent(candidates::add);
//         }
//
//         if (candidates.isEmpty()) {
//             return Optional.empty();
//         }
//
//         // If multiple soft-deleted records match, you can change this logic
//         return Optional.of(candidates.get(0));
//     }
//
//     // ───────────────────────────── UPDATE ─────────────────────────────
//
//     public Category updateCategory(Long id, Category updatedCategory, String operatedBy) {
//         return categoryRepository.findById(id).map(category -> {
//
//             // uniqueness check for update (ignore this category's own id)
//             validateUniqueFields(updatedCategory, id);
//
//             List<HistoryManagement> historyEntries = new ArrayList<>();
//
//             // helper: log change for any type field
//             BiConsumer<String, Object[]> logChange = (fieldName, values) -> {
//                 Object oldVal = values[0];
//                 Object newVal = values[1];
//
//                 if (Objects.equals(oldVal, newVal)) {
//                     return;
//                 }
//
//                 HistoryManagement history = new HistoryManagement();
//                 history.setTime(LocalDateTime.now());
//                 history.setOperationType("UPDATE");
//                 history.setOperatedBy(operatedBy);
//                 history.setOperatorId(id);
//                 history.setEntityName("VIP");
//                 history.setFieldName(fieldName);
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
//             // contactno (Long)
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
//                 boolean sameAsOld = encoder.matches(newRawPassword, category.getPassword());
//
//                 if (!sameAsOld) {
//                     // history: only mark that password updated
//                     logHistory("UPDATE", "VIP", id, operatedBy,
//                             "password", null, "UPDATED");
//
//                     category.setPassword(encoder.encode(newRawPassword));
//                 }
//             }
//
//             Category saved = categoryRepository.save(category);
//
//             if (!historyEntries.isEmpty()) {
//                 historyManagementRepository.saveAll(historyEntries);
//             }
//
//             return saved;
//
//         }).orElseThrow(() -> new RuntimeException("Category not found with id " + id));
//     }
//
//     // ───────────────────────────── DELETE (SOFT) ─────────────────────────────
//
//     public void softDeleteCategory(Long id, String operatedBy) {
//         Category category = categoryRepository.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Category not found with id " + id));
//
//
//         String oldStatus = category.getStatus();
//
//         category.setStatus(STATUS_DELETED);
//         categoryRepository.save(category);
//
//         // log delete
//         logHistory("DELETE", "VIP", id, operatedBy,
//                 "status", oldStatus, STATUS_DELETED);
//     }
//
//     // ───────────────────────────── HISTORY HELPER ─────────────────────────────
//
//     private void logHistory(String operationType,
//             String entityName,
//             Long operatorId,
//             String operatedBy,
//             String fieldName,
//             String oldValue,
//             String newValue) {
//
//         HistoryManagement history = new HistoryManagement();
//         history.setTime(LocalDateTime.now());
//         history.setOperationType(operationType);
//         history.setOperatedBy(operatedBy);
//         history.setOperatorId(operatorId);
//         history.setEntityName(entityName);
//         history.setFieldName(fieldName);
//         history.setOldValue(oldValue);
//         history.setNewValue(newValue);
//         System.out.println(history + " log history createc");
//
//         historyManagementRepository.save(history);
//     }
// }
//
//

package com.infotech.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.infotech.entity.Category;
import com.infotech.entity.HistoryManagement;
import com.infotech.exception.BadRequestException;
import com.infotech.exception.ResourceNotFoundException;
import com.infotech.repository.CategoryRepository;
import com.infotech.repository.HistoryManagementRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final String STATUS_ACTIVE = "Inactive";
    private static final String STATUS_DELETED = "Deleted";

    private final CategoryRepository categoryRepository;
    private final HistoryManagementRepository historyManagementRepository;
    private final PasswordEncoder encoder;

    // ───────────────────────────── UNIQUE FIELD VALIDATION
    // ─────────────────────────────

    /**
     * Validates that username, email and contactno are unique among all
     * NON-DELETED categories.
     *
     * @param dto      category data (create/update)
     * @param ignoreId category id to ignore (for update), or null for create.
     * @throws BadRequestException if any field already exists.
     */
    private void validateUniqueFields(Category dto, Long ignoreId) {

        // username
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            categoryRepository.findByUsernameAndStatusNot(dto.getUsername(), STATUS_DELETED)
                    .filter(c -> !Objects.equals(c.getId(), ignoreId))
                    .ifPresent(c -> {
                        throw new BadRequestException("Username already exists");
                    });
        }

        // email
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            categoryRepository.findByEmailAndStatusNot(dto.getEmail(), STATUS_DELETED)
                    .filter(c -> !Objects.equals(c.getId(), ignoreId))
                    .ifPresent(c -> {
                        throw new BadRequestException("Email already exists");
                    });
        }

        // contactno
        if (dto.getContactno() != null) {
            categoryRepository.findByContactnoAndStatusNot(dto.getContactno(), STATUS_DELETED)
                    .filter(c -> !Objects.equals(c.getId(), ignoreId))
                    .ifPresent(c -> {
                        throw new BadRequestException("Contact number already exists");
                    });
        }
    }

    // ───────────────────────────── CREATE / RESTORE ─────────────────────────────

    public Category createOrRestoreCategory(Category newCategory, String operatedBy) {

        // ✅ error handling: null body
        if (newCategory == null) {
            throw new BadRequestException("Request body cannot be null");
        }

        if (newCategory.getUsername() == null || newCategory.getUsername().isBlank()) {
            throw new BadRequestException("Username is required");
        }

        if (newCategory.getPassword() == null || newCategory.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        if (newCategory.getContactno() == null) {
            throw new BadRequestException("Contactno is required");
        }

        if (newCategory.getEmail() == null || newCategory.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (newCategory.getName() == null || newCategory.getName().isBlank()) {
            throw new BadRequestException("Name is required");
        }
        if (newCategory.getDesignation() == null || newCategory.getDesignation().isBlank()) {
            throw new BadRequestException("Designation is required");
        }

        // uniqueness check for create (ignoreId = null)
        validateUniqueFields(newCategory, null);

        // Find a soft-deleted match by username/email/contactno
        Optional<Category> softDeletedOpt = findSoftDeletedMatch(newCategory);

        if (softDeletedOpt.isPresent()) {
            Category deletedCat = softDeletedOpt.get();
            String oldStatus = deletedCat.getStatus();

            deletedCat.setStatus(STATUS_ACTIVE);
            deletedCat.setName(newCategory.getName());
            deletedCat.setEmail(newCategory.getEmail());
            deletedCat.setDesignation(newCategory.getDesignation());
            deletedCat.setContactno(newCategory.getContactno());
            deletedCat.setUsername(newCategory.getUsername());
            deletedCat.setPassword(encoder.encode(newCategory.getPassword()));

            Category restored = categoryRepository.save(deletedCat);

            // Log RESTORE
            logHistory("RESTORE", "VIP", restored.getId(), operatedBy,
                    "status", oldStatus, STATUS_ACTIVE);

            return restored;
        }

        // No soft-deleted match → create new
        newCategory.setStatus(STATUS_ACTIVE);
        newCategory.setPassword(encoder.encode(newCategory.getPassword()));

        Category saved = categoryRepository.save(newCategory);
        System.out.println(saved + "  category created ");

        // Log CREATE
        logHistory("CREATE", "VIP", saved.getId(), operatedBy,
                "status", null, STATUS_ACTIVE);

        return saved;
    }

    /**
     * Find a soft-deleted Category that matches by username OR email OR contactno.
     */
    private Optional<Category> findSoftDeletedMatch(Category newCategory) {
        List<Category> candidates = new ArrayList<>();

        if (newCategory.getUsername() != null && !newCategory.getUsername().isBlank()) {
            categoryRepository
                    .findByUsernameAndStatus(newCategory.getUsername(), STATUS_DELETED)
                    .ifPresent(candidates::add);
        }

        if (newCategory.getEmail() != null && !newCategory.getEmail().isBlank()) {
            categoryRepository
                    .findByEmailAndStatus(newCategory.getEmail(), STATUS_DELETED)
                    .ifPresent(candidates::add);
        }

        if (newCategory.getContactno() != null) { // contactno is Long
            categoryRepository
                    .findByContactnoAndStatus(newCategory.getContactno(), STATUS_DELETED)
                    .ifPresent(candidates::add);
        }

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        // If multiple soft-deleted records match, you can change this logic
        return Optional.of(candidates.get(0));
    }

    // ───────────────────────────── UPDATE ─────────────────────────────

    public Category updateCategory(Long id, Category updatedCategory, String operatedBy) {

        if (updatedCategory == null) {
            throw new BadRequestException("Request body cannot be null");
        }

        return categoryRepository.findById(id).map(category -> {

            // uniqueness check for update (ignore this category's own id)
            validateUniqueFields(updatedCategory, id);

            List<HistoryManagement> historyEntries = new ArrayList<>();

            BiConsumer<String, Object[]> logChange = (fieldName, values) -> {
                Object oldVal = values[0];
                Object newVal = values[1];

                if (Objects.equals(oldVal, newVal)) {
                    return;
                }

                HistoryManagement history = new HistoryManagement();
                history.setTime(LocalDateTime.now());
                history.setOperationType("UPDATE");
                history.setOperatedBy(operatedBy);
                history.setOperatorId(id);
                history.setEntityName("VIP");
                history.setFieldName(fieldName);
                history.setOldValue(oldVal == null ? null : oldVal.toString());
                history.setNewValue(newVal == null ? null : newVal.toString());

                historyEntries.add(history);
            };

            // 🔹 name (update only if not null/blank)
            if (updatedCategory.getName() != null && !updatedCategory.getName().isBlank()) {
                logChange.accept("name", new Object[] {
                        category.getName(),
                        updatedCategory.getName()
                });
                category.setName(updatedCategory.getName());
            }

            // 🔹 email
            if (updatedCategory.getEmail() != null && !updatedCategory.getEmail().isBlank()) {
                logChange.accept("email", new Object[] {
                        category.getEmail(),
                        updatedCategory.getEmail()
                });
                category.setEmail(updatedCategory.getEmail());
            }

            // 🔹 status
            if (updatedCategory.getStatus() != null && !updatedCategory.getStatus().isBlank()) {
                logChange.accept("status", new Object[] {
                        category.getStatus(),
                        updatedCategory.getStatus()
                });
                category.setStatus(updatedCategory.getStatus());
            }

            // 🔹 designation
            if (updatedCategory.getDesignation() != null && !updatedCategory.getDesignation().isBlank()) {
                logChange.accept("designation", new Object[] {
                        category.getDesignation(),
                        updatedCategory.getDesignation()
                });
                category.setDesignation(updatedCategory.getDesignation());
            }

            // 🔹 contactno (Long → only null check)
            if (updatedCategory.getContactno() != null) {
                logChange.accept("contactno", new Object[] {
                        category.getContactno(),
                        updatedCategory.getContactno()
                });
                category.setContactno(updatedCategory.getContactno());
            }

            // 🔹 username
            if (updatedCategory.getUsername() != null && !updatedCategory.getUsername().isBlank()) {
                logChange.accept("username", new Object[] {
                        category.getUsername(),
                        updatedCategory.getUsername()
                });
                category.setUsername(updatedCategory.getUsername());
            }

            // 🔹 password (only if user sent a new one & it's actually different)
            String newRawPassword = updatedCategory.getPassword();
            if (newRawPassword != null && !newRawPassword.isBlank()) {

                boolean sameAsOld = encoder.matches(newRawPassword, category.getPassword());

                if (!sameAsOld) {
                    logHistory("UPDATE", "VIP", id, operatedBy,
                            "password", null, "UPDATED");

                    category.setPassword(encoder.encode(newRawPassword));
                }
            }

            Category saved = categoryRepository.save(category);

            if (!historyEntries.isEmpty()) {
                historyManagementRepository.saveAll(historyEntries);
            }

            return saved;

        }).orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
    }
    // ───────────────────────────── DELETE (SOFT) ─────────────────────────────

    public void softDeleteCategory(Long id, String operatedBy) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));

        String oldStatus = category.getStatus();

        category.setStatus(STATUS_DELETED);
        categoryRepository.save(category);

        // log delete
        logHistory("DELETE", "VIP", id, operatedBy,
                "status", oldStatus, STATUS_DELETED);
    }

    // ───────────────────────────── HISTORY HELPER ─────────────────────────────

    private void logHistory(String operationType,
            String entityName,
            Long operatorId,
            String operatedBy,
            String fieldName,
            String oldValue,
            String newValue) {

        HistoryManagement history = new HistoryManagement();
        history.setTime(LocalDateTime.now());
        history.setOperationType(operationType);
        history.setOperatedBy(operatedBy);
        history.setOperatorId(operatorId);
        history.setEntityName(entityName);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        System.out.println(history + " log history created");

        historyManagementRepository.save(history);
    }
}
