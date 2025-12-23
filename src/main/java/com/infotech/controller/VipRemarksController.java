package com.infotech.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.infotech.dto.VipRemarksdto;
import com.infotech.entity.Category;
import com.infotech.entity.Officer;
import com.infotech.entity.VipRemarks;
import com.infotech.repository.CategoryRepository;
import com.infotech.repository.OfficerRepository;
import com.infotech.repository.VipRemarksRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

  @GetMapping
  public ResponseEntity<List<VipRemarks>> getAllVipRemarks() {
    return ResponseEntity.ok(vipRemarksRepository.findAll());
  }

  @PostMapping
  public String addVipRemarks(@RequestBody VipRemarksdto vipRemarks) {

    VipRemarks remarks = new VipRemarks();

    Officer officer = officerRepository.findById(vipRemarks.getOfficerId()).orElse(null);
    if (officer == null) {
      return "Officer not found";
    }

    Category vip = categoryRepository.findById(vipRemarks.getVipId()).orElse(null);
    if (categoryRepository == null) {
      return "Vip not found";
    }

    remarks.setOfficer(officer);
    remarks.setVip(vip);
    remarks.setRemarks(vipRemarks.getRemarks());
    remarks.setSubject(vipRemarks.getSubject());
    remarks.setStatus("Pending");
    remarks.setCreated(LocalDateTime.now());

    vipRemarksRepository.save(remarks);

    return "Vip Remarks added successfully";
  }
}
