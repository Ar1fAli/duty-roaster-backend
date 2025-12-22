// package com.infotech.controller;
//
// import java.util.List;
// import java.util.Optional;
//
// import com.infotech.entity.Officer;
// import com.infotech.repository.LeaveRequestRepository;
// import com.infotech.repository.OfficerRepository;
//
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
//
// import lombok.RequiredArgsConstructor;
//
// @RestController
// @RequestMapping("/api/officer")
// // @CrossOrigin(origins = "http://localhost:5173")
// @CrossOrigin(origins = "*")
// @RequiredArgsConstructor
// public class OfficerController {
//
//     private final OfficerRepository officerRepository;
//     private final LeaveRequestRepository leaveRequestRepository;
//     private final PasswordEncoder encoder;
//
//     @GetMapping
//     public List<Officer> getAllOfficer() {
//         return officerRepository.findAll();
//     }
//
//     @PostMapping
//     public Officer createCategory(@RequestBody Officer officer) {
//
//         officer.setPassword(encoder.encode(officer.getPassword()));
//         return officerRepository.save(officer);
//     }
//
//     @GetMapping("/profile")
//     public Optional<Officer> getOfficer(@RequestParam String username) {
//         Optional<Officer> admindata = officerRepository.findByUsername(username);
//         return admindata;
//     }
//
//     @PutMapping("/{id}")
//     public Officer updateOfficer(@PathVariable Long id, @RequestBody Officer updatedOfficer) {
//         return officerRepository.findById(id).map(officer -> {
//             // officer.setGuard_id(updatedOfficer.getGuard_id());
//             officer.setName(updatedOfficer.getName());
//             officer.setEmail(updatedOfficer.getEmail());
//             officer.setRank(updatedOfficer.getRank());
//             officer.setStatus(updatedOfficer.getStatus());
//             // officer.setReqstatus(updatedOfficer.getReqstatus());
//             // officer.setReasonmes(updatedOfficer.getReasonmes());
//             officer.setExperience(updatedOfficer.getExperience());
//             officer.setContactno(updatedOfficer.getContactno());
//             System.out.println("updated password " + updatedOfficer.getPassword());
//             officer.setPassword(encoder.encode(updatedOfficer.getPassword()));
//
//             System.out.println("updated password " + updatedOfficer.getPassword());
//
//             officer.setUsername(updatedOfficer.getUsername());
//
//             // Clear existing items (important for orphanRemoval = true)
//             // officer.getOfficerName().clear();
//
//             // Add updated items with proper category assignment
//             // for (OfficerName item : updatedOfficer.getOfficerName()) {
//             // item.setOfficer(officer);
//             // officer.getOfficerName().add(item);
//             // }
//
//             return officerRepository.save(officer);
//         }).orElseThrow(() -> new RuntimeException("Category not found with id " + id));
//     }
//
//     @DeleteMapping("/{id}")
//     public void deleteOfficer(@PathVariable Long id) {
//         Officer cat = officerRepository.findById(id).map(category -> {
//             category.setStatus("Deleted");
//             return officerRepository.save(category);
//         }).orElseThrow(() -> new RuntimeException("Category not found with id " + id));
//         System.out.println("data deleted Successfully");
//     }
// }

package com.infotech.controller;

import java.util.List;

import com.infotech.dto.GuardProfileResponse;
import com.infotech.dto.OfficerRequestDto;
import com.infotech.dto.OfficerResponseDto;
import com.infotech.entity.Officer;
import com.infotech.repository.LeaveRequestRepository;
import com.infotech.repository.OfficerRepository;
import com.infotech.service.OfficerService;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/officer")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OfficerController {

  private final OfficerService officerService;
  private final LeaveRequestRepository leaveRequestRepository; // keep if used elsewhere
  private final OfficerRepository officerRepository;

  // In real app, read from SecurityContext
  private String getCurrentOperator() {
    return "Guard";
  }

  // LIST with pagination & filters
  @GetMapping
  public Page<OfficerResponseDto> getOfficers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String rank) {

    return officerService.getOfficers(page, size, status, rank);
  }

  // GET by username (profile)
  @GetMapping("/profile")
  public GuardProfileResponse getOfficer(@RequestParam String username) {
    Officer offi = officerRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("user name not found"));
    GuardProfileResponse res = new GuardProfileResponse();
    res.setId(offi.getId());
    res.setUsername(offi.getUsername());
    res.setName(offi.getName());
    res.setEmail(offi.getEmail());
    res.setStatus(offi.getStatus());
    res.setContactno(offi.getContactno());
    res.setRank(offi.getRank());
    if (offi.getPic() != null) {
      res.setUrl(offi.getPic().getUrl());
    }
    res.setExperience(offi.getExperience());
    res.setAdharNo(offi.getAdharNo());
    res.setPnNumber(offi.getPnNumber());
    res.setGender(offi.getGender());
    return res;

  }

  // GET by id (optional)
  @GetMapping("/{id}")
  public ResponseEntity<OfficerResponseDto> getOfficerById(@PathVariable Long id) {
    return ResponseEntity.ok(officerService.getByIdDto(id));
  }

  // CREATE or RESTORE
  @PostMapping("/register/{role}")
  public ResponseEntity<OfficerResponseDto> createOfficer(@RequestBody OfficerRequestDto officerDto,
      @PathVariable String role) {
    OfficerResponseDto saved = officerService.createOrRestoreOfficer(officerDto, role);
    return ResponseEntity.ok(saved);
  }

  // UPDATE
  @PutMapping("/{id}/{role}")
  public ResponseEntity<OfficerResponseDto> updateOfficer(@PathVariable Long id,
      @RequestBody OfficerRequestDto updatedDto, @PathVariable String role) {
    // updatedDto.setCreatedTime(LocalDateTime.now());
    OfficerResponseDto updated = officerService.updateOfficer(id, updatedDto, role);
    return ResponseEntity.ok(updated);
  }

  // SOFT DELETE
  @DeleteMapping("/{id}/{role}")
  public ResponseEntity<Void> deleteOfficer(@PathVariable Long id, @PathVariable String role) {
    officerService.softDeleteOfficer(id, role);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/unique-ranks")
  public ResponseEntity<List<String>> getUniqueRanks() {
    return ResponseEntity.ok(officerService.totalRank());
  }
}
