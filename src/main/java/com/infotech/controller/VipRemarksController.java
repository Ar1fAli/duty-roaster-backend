package com.infotech.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.infotech.dto.VipRemarksdto;
import com.infotech.entity.Category;
import com.infotech.entity.Officer;
import com.infotech.entity.UserGuardAssignment;
import com.infotech.entity.VipRemarks;
import com.infotech.repository.CategoryRepository;
import com.infotech.repository.OfficerRepository;
import com.infotech.repository.UserGuardAssignmentRepository;
import com.infotech.repository.VipRemarksRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vipremarks")
public class VipRemarksController {

  private final VipRemarksRepository vipRemarksRepository;
  private final CategoryRepository categoryRepository;
  private final OfficerRepository officerRepository;
  private final UserGuardAssignmentRepository currentassignmentRepo;

  @GetMapping
  public ResponseEntity<List<VipRemarks>> getAllVipRemarks() {
    return ResponseEntity.ok(vipRemarksRepository.findAll());
  }

  @PostMapping
  public String addVipRemarks(@RequestBody VipRemarksdto vipRemarks) {

    VipRemarks remarks = new VipRemarks();

    // List<Officer> officers = vipRemarks.getOfficers();

    List<Officer> officers2 = vipRemarks.getOfficerId().stream()
        .map(officerRepository::findById)
        .flatMap(Optional::stream)
        .collect(Collectors.toList());
    Long curraAssignment = vipRemarks.getCurrentAssignmentId() == null ? null : vipRemarks.getCurrentAssignmentId();
    if (curraAssignment == null) {
      return "Assignment not found";
      // Optional<UserGuardAssignment> currentAssignment =
      // currentassignmentRepo.findById(curraAssignment);

    }
    UserGuardAssignment assignment = currentassignmentRepo.findById(curraAssignment)
        .orElseThrow(() -> new RuntimeException("Assignment not found"));

    if (vipRemarks.getOfficerId() == null || vipRemarks.getOfficerId().isEmpty()) {
      remarks.setOfficersRemarks(assignment.getOfficers());

    }

    remarks.setOfficersRemarks(officers2);
    remarks.setAssignment(assignment);
    // remarks.setVip(vip);
    remarks.setRemarks(vipRemarks.getRemarks());
    remarks.setSubject(vipRemarks.getSubject());
    remarks.setStatus("Pending");
    remarks.setCreated(LocalDateTime.now());

    Category cat = categoryRepository.findById(vipRemarks.getVipId())
        .orElseThrow(() -> new RuntimeException(
            "Category not found for id: " + vipRemarks.getVipId()));
    remarks.setCategory(cat);

    vipRemarksRepository.save(remarks);

    return "Vip Remarks added successfully";
  }

  @GetMapping("/{id}")
  public List<VipRemarks> getVipRemarks(@PathVariable Long id) {
    return vipRemarksRepository.findByCategory_Id(id);
    // TODO Auto-generated constructor stub
  }
}
