// package com.infotech.config;
//
// import com.infotech.entity.Officer;
// import com.infotech.repository.OfficerRepository;
//
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.crypto.password.PasswordEncoder;
//
// @Configuration
// public class OfficerDataSeeder {
//
// @Bean
// public CommandLineRunner seedOfficers(OfficerRepository officerRepository,
// PasswordEncoder passwordEncoder) {
// return args -> {
//
// // If already seeded once, don't insert again
// if (officerRepository.count() >= 500) {
// return;
// }
//
// String[] firstNames = {
// "Arjun", "Rohan", "Vikas", "Manish", "Avinash", "Sumit", "Karan", "Rahul",
// "Neeraj", "Yogesh",
// "Sagar", "Mukesh", "Hemant", "Deepak", "Arvind", "Tarun", "Sachin", "Ajay",
// "Dinesh", "Lalit",
// "Rakesh", "Gaurav", "Pawan", "Suresh", "Kamal", "Chandan", "Dhruv",
// "Pradeep", "Umesh", "Aditya",
// "Ashok", "Mahesh", "Suraj", "Rajesh", "Vishal", "Nand", "Dev", "Paramjeet",
// "Ravi", "Sarvesh",
// "Bhavesh", "Dharam", "Sushil", "Tarachand", "Jeet", "Abhinav", "Samar",
// "Aryan", "Harsh", "Kunal"
// };
//
// String[] lastNames = {
// "Mehra", "Singh", "Shah", "Patel", "Kumar", "Tomar", "Arora", "Bhatt",
// "Saxena", "Nair",
// "Malhotra", "Rana", "Verma", "Sharma", "Jain", "Bedi", "Gupta", "Chopra",
// "Bora", "Mishra",
// "Yadav", "Pillai", "Joshi", "Agarwal", "Sen", "Modi", "Suryavanshi",
// "Kapoor", "Gera", "Trivedi",
// "Menon", "Raju", "Khanna", "Kulkarni", "Singla", "Kashyap", "Chatterjee",
// "Sabharwal", "Tiwari",
// "Bhalla", "Pal", "Raichand", "Arvind", "Rajput", "More", "Bhardwaj", "Kohli",
// "Grover", "Kaul",
// "Soni"
// };
//
// long baseContact = 9810001001L; // 10-digit contact numbers start here
//
// for (int i = 1; i <= 500; i++) {
//
// // ----- Rank based on index range -----
// String rank;
// if (i <= 100) {
// rank = "A Grade"; // lowest numbers
// } else if (i <= 200) {
// rank = "B Grade";
// } else if (i <= 300) {
// rank = "C Grade";
// } else if (i <= 400) {
// rank = "D Grade";
// } else {
// rank = "E Grade"; // highest numbers
// }
//
// // ----- Unique Name -----
// String firstName = firstNames[(i - 1) % firstNames.length];
// String lastName = lastNames[(i - 1) % lastNames.length];
// String fullName = firstName + " " + lastName + " " + i; // make it unique
//
// // ----- Username & email -----
// String baseUsername = (firstName + i).toLowerCase();
// String username = baseUsername;
//
// // requires: boolean existsByUsername(String username) in OfficerRepository
// if (officerRepository.existsByUsername(username)) {
// username = (firstName + "_" + lastName + "_" + i).toLowerCase();
// }
//
// String emailLocalPart = (firstName + "." + lastName + "." + i).toLowerCase();
// String email = emailLocalPart + "@example.com";
//
// // ----- Contact & experience -----
// Long contactNo = baseContact + (i - 1); // Long type
// Long experience = (long) ((i - 1) % 20 + 1); // 1–20 years as Long
//
// // ----- Status (always Inactive) -----
// String status = "Inactive";
//
// // ----- Password -----
// String rawPassword = "Officer@" + i;
// String encodedPassword = passwordEncoder.encode(rawPassword);
//
// Officer officer = new Officer();
//
// // If your @Id is @GeneratedValue, DO NOT set id manually.
//
// officer.setContactno(contactNo);
// officer.setExperience(experience);
// officer.setEmail(email);
// officer.setName(fullName);
// officer.setRank(rank);
// officer.setStatus(status);
// officer.setUsername(username);
// officer.setPassword(encodedPassword);
//
// officerRepository.save(officer);
// }
// };
// }
// }
