// package com.infotech.repository;
//
// import java.util.List;
// import java.util.Optional;
//
// import com.infotech.entity.Officer;
//
// import org.springframework.data.jpa.repository.JpaRepository;
//
// public interface OfficerRepository extends JpaRepository<Officer, Long> {
//
// List<Officer> findByRank(String rank);
//
// Optional<Officer> findByUsername(String username);
//
// boolean existsByUsername(String username);
//
// }

package com.infotech.repository;

import java.util.List;
import java.util.Optional;

import com.infotech.entity.Officer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficerRepository extends JpaRepository<Officer, Long> {

    Optional<Officer> findByUsername(String username);

    // For restore logic (soft-deleted officers)
    Optional<Officer> findByUsernameAndStatus(String username, String status);

    Optional<Officer> findByEmailAndStatus(String email, String status);

    Optional<Officer> findByContactnoAndStatus(Long contactno, String status);

    // For list filters
    Page<Officer> findByStatus(String status, Pageable pageable);

    Page<Officer> findByRank(String rank, Pageable pageable);

    Page<Officer> findByStatusAndRank(String status, String rank, Pageable pageable);

    List<Officer> findByRank(String rank);

    boolean existsByUsername(String username);

}
