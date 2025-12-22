package com.infotech.controller;

import java.util.List;

import jakarta.transaction.Transactional;

import com.infotech.entity.AssignmentValue;
import com.infotech.entity.SecurityType;
import com.infotech.repository.AssignmentValueRepository;
import com.infotech.repository.SecurityTypeRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/setting")
public class SettingController {
  private final SecurityTypeRepository securityTypeRepository;
  private final AssignmentValueRepository assignmentValueRepository;

  @PostMapping("/set")
  @Transactional
  public void setSecurityType(@RequestBody SecurityType req) {

    SecurityType sec = new SecurityType();
    sec.setName(req.getName());

    // ✅ SAVE PARENT FIRST
    SecurityType sec2 = securityTypeRepository.save(sec);

    req.getValues().forEach(value -> {
      AssignmentValue av = new AssignmentValue();
      av.setValue(value.getValue());
      av.setRank(value.getRank());
      av.setType(sec2); // now managed entity
      assignmentValueRepository.save(av);
    });
  }

  @GetMapping("/getsecuritygroup")
  public List<SecurityType> getAllSecurityTypes() {
    List<SecurityType> securityType = securityTypeRepository.findAll();
    return securityType;
  }

  @GetMapping("/getsecuritygroup/{id}")
  public SecurityType getSecurityType(Long id) {
    return securityTypeRepository.findById(id).get();
  }

}
