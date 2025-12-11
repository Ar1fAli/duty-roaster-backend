package com.infotech.service;
//

// import java.util.Optional;
//
// import jakarta.transaction.Transactional;
//
// import com.infotech.dto.LoginResponse;
// import com.infotech.dto.Logindat;
// import com.infotech.entity.AdminEntity;
// import com.infotech.repository.AdminRepsitory;
//
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Service;
//
// import lombok.RequiredArgsConstructor;
//
// @Service
// @RequiredArgsConstructor
// @Transactional
// public class AdminServiceImp implements AdminService {
//
//     private final AdminRepsitory adminRepsitory;
//     private final PasswordEncoder encoder;
//     private final AuthenticationManager authManager;
//     private final JwtService jwtService;
//
//     @Override
//     public String register(AdminEntity adminEntity) {
//         System.out.println(adminEntity.getAdminPassword());
//         if (adminRepsitory.findByAdminUsername(adminEntity.getAdminUsername()).isPresent())
//             return "Username already exists";
//
//         adminEntity.setAdminPassword(encoder.encode(adminEntity.getAdminPassword()));
//         adminRepsitory.save(adminEntity);
//         return "User registered successfully";
//     }
//
//     @Override
//     public Optional<AdminEntity> getAdmin(String userName) {
//         Optional<AdminEntity> admindata = adminRepsitory.findByAdminUsername(userName);
//         return admindata;
//     }
//
//     @Override
//     public LoginResponse login(Logindat loginDto) {
//         try {
//             System.out.println(loginDto.getUsername());
//             System.out.println(loginDto.getPassword());
//
//             // This will call CustomUserDetailsService.loadUserByUsername()
//             Authentication authentication = authManager.authenticate(
//                     new UsernamePasswordAuthenticationToken(
//                             loginDto.getUsername(),
//                             loginDto.getPassword()));
//
//             System.out.println("working");
//
//             // Extract role from granted authorities
//             String fullRole = authentication.getAuthorities()
//                     .stream()
//                     .findFirst()
//                     .orElseThrow(() -> new RuntimeException("No role found"))
//                     .getAuthority(); // e.g. "ROLE_ADMIN"
//
//             // Optional: strip "ROLE_"
//             String role = fullRole.replace("ROLE_", ""); // ADMIN / GUARD / VIP
//
//             LoginResponse res = new LoginResponse();
//             // If your JWT should also contain role, adjust generateToken accordingly
//             res.setData(jwtService.generateToken(loginDto.getUsername()));
//             res.setRole(role);
//
//             return res;
//
//         } catch (Exception e) {
//             System.out.println("Login failed: " + e.getMessage());
//             throw new RuntimeException("Invalid username or password");
//         }
//     }
//
//     // @Override
//     // public LoginResponse login(Logindat adminEntity) {
//     // try {
//     // System.out.println(adminEntity.getUsername());
//     // System.out.println(adminEntity.getPassword());
//     //
//     // authManager.authenticate(
//     // new UsernamePasswordAuthenticationToken(
//     // adminEntity.getUsername(),
//     // adminEntity.getPassword()));
//     //
//     // System.out.println("working");
//     // AdminEntity admin =
//     // adminRepsitory.findByAdminUsername(adminEntity.getUsername())
//     // .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//     // LoginResponse res = new LoginResponse();
//     // res.setData(jwtService.generateToken(adminEntity.getUsername()));
//     // res.setRole(admin.getRole());
//     // return res;
//     //
//     // } catch (Exception e) {
//     // System.out.println("Login failed: " + e.getMessage());
//     // throw new RuntimeException("Invalid username or password");
//     // }
//     // }
//
//     public AdminEntity updateCategory(Long id, AdminEntity admindat) {
//
//         return adminRepsitory.findById(id).map(admin -> {
//             admin.setId(admindat.getId());
//             admin.setAdminName(admindat.getAdminName());
//             admin.setAdminUsername(admindat.getAdminUsername());
//             admin.setAdminEmail(admindat.getAdminEmail());
//             admin.setAdminPassword(encoder.encode(admindat.getAdminPassword()));
//             admin.setContactNo(admindat.getContactNo());
//             admin.setRole(admindat.getRole());
//
//             return adminRepsitory.save(admin);
//         }).orElseThrow(() -> new RuntimeException("Category not found with id " +
//                 id));
//     }
//
// }

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import com.infotech.dto.AdminRequestDto;
import com.infotech.dto.LoginResponse;
import com.infotech.dto.Logindat;
import com.infotech.entity.AdminEntity;
import com.infotech.entity.HistoryManagement;
import com.infotech.entity.UserData;
import com.infotech.repository.AdminRepsitory;
import com.infotech.repository.HistoryManagementRepository;
import com.infotech.repository.UserDataRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImp implements AdminService {

  private final AdminRepsitory adminRepsitory;
  private final PasswordEncoder encoder;
  private final AuthenticationManager authManager;
  private final JwtService jwtService;
  private final UserDataRepository userDataRepository;

  // NEW: inject history repo
  private final HistoryManagementRepository historyManagementRepository;

  @Override
  public String register(AdminRequestDto adminEntity) {

    UserData userd = new UserData();
    AdminEntity admin = new AdminEntity();
    if (adminEntity.getAdminName() != null && !adminEntity.getAdminName().isBlank()) {
      admin.setAdminName(adminEntity.getAdminName());
    }
    if (adminEntity.getAdminEmail() != null && !adminEntity.getAdminEmail().isBlank()) {
      admin.setAdminEmail(adminEntity.getAdminEmail());
    }
    if (adminEntity.getContactNo() != null) {
      admin.setContactNo(adminEntity.getContactNo());
    }
    if (adminEntity.getRole() != null && !adminEntity.getRole().isBlank()) {
      userd.setRole(adminEntity.getRole());

    }

    // System.out.println("Admin Entity: " + adminEntity);
    if (userDataRepository.findByUsername(adminEntity.getAdminUsername()).isPresent())
      return "Username already exists";
    if (adminRepsitory.findByAdminEmail(adminEntity.getAdminEmail()).isPresent())
      return "Email already exists";
    userd.setPassword(encoder.encode(adminEntity.getAdminPassword()));
    userd.setUsername(adminEntity.getAdminUsername());

    userd = userDataRepository.save(userd);

    admin.setUserData(userd);
    adminRepsitory.save(admin);
    return "User registered successfully";
  }

  @Override
  public Optional<AdminEntity> getAdmin(String userName) {
    Optional<AdminEntity> admindata = adminRepsitory.findByUserData_Username(userName);
    return admindata;
  }

  @Override
  public LoginResponse login(Logindat loginDto) {
    try {
      System.out.println(loginDto.getUsername());
      System.out.println(loginDto.getPassword());

      Authentication authentication = authManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginDto.getUsername(),
              loginDto.getPassword()));

      System.out.println("working");

      String fullRole = authentication.getAuthorities()
          .stream()
          .findFirst()
          .orElseThrow(() -> new RuntimeException("No role found"))
          .getAuthority(); // e.g. ROLE_ADMIN

      String role = fullRole.replace("ROLE_", ""); // ADMIN / GUARD / VIP

      LoginResponse res = new LoginResponse();
      res.setData(jwtService.generateToken(loginDto.getUsername()));
      res.setRole(role);
      res.setUsername(loginDto.getUsername());

      return res;

    } catch (Exception e) {
      System.out.println("Login failed: " + e.getMessage());
      throw new RuntimeException("Invalid username or password");
    }
  }

  // ✅ UPDATED: history + validation + null-safe update
  public String updateCategory(Long id, AdminRequestDto admindat, String role) {

    return adminRepsitory.findById(id).map(admin -> {

      // who is performing the operation? (you can adjust this later)
      String operatedBy = role; // or "SYSTEM" or from token

      // --- uniqueness check for username (if changed) ---
      if (admindat.getAdminUsername() != null && !admindat.getAdminUsername().isBlank()) {
        adminRepsitory.findByUserData_Username(admindat.getAdminUsername())
            .filter(existing -> !Objects.equals(existing.getId(), id))
            .ifPresent(existing -> {
              // this will be caught in controller and sent as 400
              throw new IllegalStateException("Username already exists");
            });
        // adminRepsitory.findByAdminUsername(admindat.getAdminUsername())
        // .filter(existing -> !Objects.equals(existing.getId(), id))
        // .ifPresent(existing -> {
        // // this will be caught in controller and sent as 400
        // throw new IllegalStateException("Username already exists");
        // });
      }

      List<HistoryManagement> historyEntries = new ArrayList<>();

      BiConsumer<String, Object[]> logChange = (fieldName, values) -> {
        Object oldVal = values[0];
        Object newVal = values[1];

        if (Objects.equals(oldVal, newVal)) {
          return; // no change, no history
        }

        HistoryManagement history = new HistoryManagement();
        history.setTime(LocalDateTime.now());
        history.setOperationType("UPDATE");
        history.setOperatedBy(operatedBy);
        history.setOperatorId(id);
        history.setEntityName("Admin");
        history.setFieldName(fieldName);
        history.setOldValue(oldVal == null ? null : oldVal.toString());
        history.setNewValue(newVal == null ? null : newVal.toString());

        historyEntries.add(history);
      };

      // ❌ DO NOT override ID from request
      // admin.setId(admindat.getId());

      // ============== NULL-SAFE FIELD UPDATES + HISTORY ==============

      // name
      if (admindat.getAdminName() != null && !admindat.getAdminName().isBlank()) {
        logChange.accept("adminName", new Object[] {
            admin.getAdminName(),
            admindat.getAdminName()
        });
        admin.setAdminName(admindat.getAdminName());
      }

      // email
      if (admindat.getAdminEmail() != null && !admindat.getAdminEmail().isBlank()) {
        logChange.accept("adminEmail", new Object[] {
            admin.getAdminEmail(),
            admindat.getAdminEmail()
        });
        admin.setAdminEmail(admindat.getAdminEmail());
      }

      // contactNo (no blank check if it's a number, just null check)
      if (admindat.getContactNo() != null) {
        logChange.accept("contactNo", new Object[] {
            admin.getContactNo(),
            admindat.getContactNo()
        });
        admin.setContactNo(admindat.getContactNo());
      }

      UserData userdat = userDataRepository.findById(admin.getUserData().getId())
          .orElseThrow(() -> new EntityNotFoundException("User not found"));
      // role
      // if (admindat.getRole() != null && !admindat.getRole().isBlank()) {
      // logChange.accept("role", new Object[] {
      // admin.getRole(),
      // admindat.getRole()
      // });
      // userdat.setRole(admindat.getRole());
      //
      // // admin.setRole(admindat.getRole());
      // }
      // username
      //

      if (admindat.getAdminUsername() != null && !admindat.getAdminUsername().isBlank()) {
        logChange.accept("adminUsername", new Object[] {
            // admin.getAdminUsername(),
            userdat.getUsername(),
            admindat.getAdminUsername()
        });
        userdat.setUsername(admindat.getAdminUsername());
        // admin.setAdminUsername(admindat.getAdminUsername());
      }

      // ============== PASSWORD (special) ==============
      String newRawPassword = admindat.getAdminPassword();
      System.out.println(newRawPassword + " new and old password " + admin.getUserData().getPassword());
      if (newRawPassword != null && !newRawPassword.isBlank()) {

        boolean sameAsOld = encoder.matches(newRawPassword, admin.getUserData().getPassword());

        if (!sameAsOld) {
          // don't log real password
          HistoryManagement history = new HistoryManagement();
          history.setTime(LocalDateTime.now());
          history.setOperationType("UPDATE");
          history.setOperatedBy(operatedBy);
          history.setOperatorId(id);
          history.setEntityName("Admin");
          history.setFieldName("adminPassword");
          history.setOldValue(null);
          history.setNewValue("UPDATED");

          historyEntries.add(history);

          // admin.setAdminPassword(encoder.encode(newRawPassword));
          userdat.setPassword(encoder.encode(newRawPassword));
        }
      }

      // userdat.setId(admin.getUserData().getId());
      userDataRepository.save(userdat);
      admin.setUserData(userdat);
      AdminEntity saved = adminRepsitory.save(admin);

      if (!historyEntries.isEmpty()) {
        historyManagementRepository.saveAll(historyEntries);
      }

      return "register successfully";

    }).orElseThrow(() -> new EntityNotFoundException("Admin not found with id " + id));
  }

}
