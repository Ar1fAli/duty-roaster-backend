// package com.infotech.repository;
//
// import java.util.Optional;
//
// import com.infotech.entity.Category;
//
// import org.springframework.data.jpa.repository.JpaRepository;
//
// public interface CategoryRepository extends JpaRepository<Category, Long> {
//
// }

package com.infotech.repository;

import java.util.Optional;

import com.infotech.entity.Category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Soft-deleted lookups
    Optional<Category> findByUsernameAndStatus(String username, String status);

    Optional<Category> findByEmailAndStatus(String email, String status);

    Optional<Category> findByContactnoAndStatus(Long contactno, String status);

    // (Optional) active uniqueness checks if you want to validate before create
    boolean existsByUsernameAndStatus(String username, String status);

    boolean existsByEmailAndStatus(String email, String status);

    boolean existsByContactnoAndStatus(Long contactno, String status);

    Optional<Category> findByUsername(String username);

    boolean existsByUsername(String username);

}
