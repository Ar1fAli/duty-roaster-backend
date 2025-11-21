package com.infotech.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long contactno;

    private String name;
    private String email;
    private String designation;
    private String status;

    //
    // @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval =
    // true)
    // @JsonManagedReference
    // private List<DataItem> dataItems = new ArrayList<>();
    //
    // getters and setters

    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private Long id;
    //
    // @Column(nullable = false, unique = true)
    // private String name;
    //
    // private String type;
    //
    // // @Column(length = 255)
    // // private String description;
    //
    // public Long getId() {
    // return id;
    // }
    //
    // public void setId(Long id) {
    // this.id = id;
    // }
    //
    // public String getType() {
    // return type;
    // }
    //
    // public void setType(String type) {
    // this.type = type;
    // }
    //
    // public String getName() {
    // return name;
    // }
    //
    // public void setName(String name) {
    // this.name = name;
    // }
    //
    // public String getDescription() { return description; }
    // public void setDescription(String description) { this.description =
    // description; }
}
