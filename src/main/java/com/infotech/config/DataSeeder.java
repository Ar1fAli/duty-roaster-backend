// package com.infotech.config;
//
// import com.infotech.entity.Category;
// import com.infotech.repository.CategoryRepository;
//
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.crypto.password.PasswordEncoder;
//
// @Configuration
// public class DataSeeder {
//
// @Bean
// public CommandLineRunner seedCategories(CategoryRepository
// categoryRepository,
// PasswordEncoder passwordEncoder) {
// return args -> {
// String[] firstNames = {
// "Ranveer", "Rohit", "Arjun", "Rahul", "Aman", "Virat", "Rishabh", "Deepak",
// "Sidharth", "Kunal",
// "Harsh", "Karan", "Aditya", "Shubham", "Kabir", "Neeraj", "Ishaan", "Pranav",
// "Lakshya", "Divyansh",
// "Yuvraj", "Manish", "Saurabh", "Aniket", "Abhay", "Chetan", "Darshan",
// "Faizal", "Gaurav", "Harshit"
// };
//
// String[] lastNames = {
// "Malhotra", "Sharma", "Verma", "Singh", "Kapoor", "Kumar", "Mehta", "Soni",
// "Arora", "Yadav",
// "Patel", "Chauhan", "Gill", "Narang", "Joshi", "Bansal", "Trivedi", "Nair",
// "Reddy", "Iyer"
// };
//
// String[] designations = {
// "Bollywood Actor", "Cricketers", "Chessmaster", "User"
// };
//
// long startId = 16L;
// long startContact = 9810001001L;
// int totalUsers = 250;
//
// for (int i = 0; i < totalUsers; i++) {
// long id = startId + i;
// long contactNo = startContact + i;
//
// String firstName = firstNames[i % firstNames.length];
// String lastName = lastNames[i % lastNames.length];
//
// // UNIQUE NAME
// String fullName = firstName + " " + lastName + " " + (i + 1);
//
// String designation = designations[i % designations.length];
//
// String emailLocal = firstName.toLowerCase() + "." +
// designation.split(" ")[0].toLowerCase() + id;
// String email = emailLocal + "@example.com";
//
// // ALWAYS INACTIVE
// String status = "Inactive";
//
// String username = (firstName + id).toLowerCase();
//
// String rawPassword = "User@" + id;
// String encodedPassword = passwordEncoder.encode(rawPassword);
//
// Category category = new Category();
// category.setId(id);
// category.setContactno(contactNo);
// category.setDesignation(designation);
// category.setEmail(email);
// category.setName(fullName);
// category.setStatus(status);
// category.setUsername(username);
// category.setPassword(encodedPassword);
//
// if (!categoryRepository.existsByUsername(username)) {
// categoryRepository.save(category);
// }
// }
// };
// }
// }
