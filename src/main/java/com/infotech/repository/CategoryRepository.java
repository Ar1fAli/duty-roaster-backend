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
//     // Soft-deleted lookups
//     Optional<Category> findByUsernameAndStatus(String username, String status);
//
//     Optional<Category> findByEmailAndStatus(String email, String status);
//
//     Optional<Category> findByContactnoAndStatus(Long contactno, String status);
//
//     // (Optional) active uniqueness checks if you want to validate before create
//     boolean existsByUsernameAndStatus(String username, String status);
//
//     boolean existsByEmailAndStatus(String email, String status);
//
//     boolean existsByContactnoAndStatus(Long contactno, String status);
//
// Optional<Category> findByUsername(String username);
//
//     boolean existsByUsername(String username);
//
// }
//
//
//
//

package com.infotech.repository;

import java.util.Optional;

import com.infotech.entity.Category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Basic lookups (used for restore/soft-delete detection)
    Optional<Category> findByUsernameAndStatus(String username, String status);

    Optional<Category> findByEmailAndStatus(String email, String status);

    Optional<Category> findByContactnoAndStatus(Long contactno, String status);

    // Lookups that ignore a given status (used by uniqueness checks to ignore
    // soft-deleted rows)
    Optional<Category> findByUsernameAndStatusNot(String username, String status);

    Optional<Category> findByEmailAndStatusNot(String email, String status);

    Optional<Category> findByContactnoAndStatusNot(Long contactno, String status);

    // Convenience boolean existence checks (optional)
    boolean existsByUsernameAndStatusNot(String username, String status);

    boolean existsByEmailAndStatusNot(String email, String status);

    boolean existsByContactnoAndStatusNot(Long contactno, String status);

    Optional<Category> findByUsername(String username);
}
