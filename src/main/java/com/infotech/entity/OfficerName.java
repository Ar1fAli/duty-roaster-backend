
package com.infotech.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class OfficerName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String Name;
    // private String description;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "officer_id")
    // @JsonBackReference
    // private Officer officer;
    //
    // getters and setters
}
