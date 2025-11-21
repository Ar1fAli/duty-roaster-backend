package com.infotech.repository;

import java.util.List;

import com.infotech.entity.Category;
import com.infotech.entity.UserGuardAssignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserGuardAssignmentRepository extends JpaRepository<UserGuardAssignment, Long> {

    // All assignments for a category
    List<UserGuardAssignment> findByCategory(Category category);

    // Active assignments for a category and level (rank)
    @Query("SELECT a FROM UserGuardAssignment a JOIN a.officer o " +
            "WHERE a.category = :category AND o.rank = :level AND a.status = :status")
    List<UserGuardAssignment> findByCategoryAndGuardLevelAndStatus(
            @Param("category") Category category,
            @Param("level") String level,
            @Param("status") String status);

    // All previous assignments (any status) for a category and rank
    @Query("SELECT a FROM UserGuardAssignment a JOIN a.officer o " +
            "WHERE a.category = :category AND o.rank = :level")
    List<UserGuardAssignment> findByCategoryAndGuardLevel(
            @Param("category") Category category,
            @Param("level") String level);
}
