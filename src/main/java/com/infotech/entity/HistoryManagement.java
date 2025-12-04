package com.infotech.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Data;

@Entity
@Data
public class HistoryManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime time;

    // "CREATE", "UPDATE", "DELETE", "RESTORE"
    private String operationType;

    // e.g. "Guard" or username from SecurityContext
    private String operatedBy;

    // ID of the entity on which operation is performed (here: Category.id)
    private Long operatorId;

    // e.g. "Category"
    private String entityName;

    // e.g. "name", "email", "status", "password"
    private String fieldName;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;
}
