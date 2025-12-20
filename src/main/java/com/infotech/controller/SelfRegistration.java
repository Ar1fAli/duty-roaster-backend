package com.infotech.controller;

import com.infotech.repository.HistoryManagementRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/self")
@RequiredArgsConstructor
public class SelfRegistration {
  private final HistoryManagementRepository historyManagementRepository;

  @GetMapping
  public String selfRegistrationData() {

    return "hello";

  }

}
