package com.infotech.repository;

import java.util.Optional;

import com.infotech.entity.Category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByUsername(String username);

    boolean existsByUsername(String username);
}
