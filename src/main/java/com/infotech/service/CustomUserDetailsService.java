package com.infotech.service;
//

import java.util.Optional;

import com.infotech.entity.UserData;

// import com.infotech.entity.AdminEntity;
// import com.infotech.repository.AdminRepsitory;
//
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.stereotype.Service;
//
// import lombok.RequiredArgsConstructor;
//
// @Service
// @RequiredArgsConstructor
// public class CustomUserDetailsService implements UserDetailsService {
//
//     private final AdminRepsitory adminRepsitory;
//
//     @Override
//     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//         AdminEntity admin = adminRepsitory.findByAdminUsername(username)
//                 .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//         return org.springframework.security.core.userdetails.User.builder()
//                 .username(admin.getAdminUsername())
//                 .password(admin.getAdminPassword()) // must be BCrypt hash
//                 .roles(admin.getRole())
//                 .build();
//     }
// }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//

import com.infotech.repository.AdminRepsitory;
import com.infotech.repository.CategoryRepository;
import com.infotech.repository.OfficerRepository;
import com.infotech.repository.UserDataRepository;
import com.infotech.repository.UserRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final AdminRepsitory adminRepsitory;
  private final OfficerRepository officerRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;
  private final UserDataRepository userDataRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    Optional<UserData> user = userDataRepository.findByUsername(username);

    if (user.isPresent()) {

      UserData userData = user.get(); // <-- extract the entity

      // System.out.println(userData.getUsername());
      // System.out.println(userData.getPassword());

      return buildUser(
          userData.getUsername(),
          userData.getPassword(), // BCrypt hash
          userData.getRole().toUpperCase());// or admin.getRole());
    }

    // 1. Try admin table
    // var adminOpt = adminRepsitory.findByAdminUsername(username);
    // if (adminOpt.isPresent()) {
    // var admin = adminOpt.get();
    //
    // System.out.println();
    // System.out.println(admin.getAdminUsername());
    //
    // System.out.println();
    // return buildUser(
    // admin.getAdminUsername(),
    // admin.getAdminPassword(), // BCrypt hash
    // "ADMIN"
    // // admin.getRole()// or admin.getRole()
    // );
    // }
    //
    // 2. Try guard/officer table
    var officerOpt = officerRepository.findByUsername(username);
    if (officerOpt.isPresent()) {
      var officer = officerOpt.get();
      if (!officer.getStatus().equals("self")) {
        return buildUser(
            officer.getUsername(),
            officer.getPassword(),
            "GUARD" // or officer.getRole()
        );

      }
    }

    // 3. Try VIP / category table
    var catOpt = categoryRepository.findByUsername(username);
    if (catOpt.isPresent()) {
      var cat = catOpt.get();
      if (!cat.getStatus().equals("self")) {
        return buildUser(
            cat.getUsername(),
            cat.getPassword(),
            "VIP" // or cat.getRole()
        );

      }
    }

    // 3. Try VIP / category table
    var userrep = userRepository.findByUsername(username);
    if (userrep.isPresent()) {
      var usr = userrep.get();
      return buildUser(
          usr.getUsername(),
          usr.getPassword(),
          "USER" // or cat.getRole()
      );
    }

    throw new UsernameNotFoundException("User not found with username: " + username);
  }

  private UserDetails buildUser(String username, String password, String role) {

    // System.out.println(username);
    // System.out.println(password);
    // System.out.println(role);
    return User.builder()
        .username(username)
        .password(password) // must be encoded
        .roles(role) // "ADMIN" -> ROLE_ADMIN, etc.
        .build();
  }
}
