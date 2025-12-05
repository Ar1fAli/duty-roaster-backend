package com.infotech.controller;

import java.util.List;

import com.infotech.entity.HistoryManagement;
import com.infotech.repository.HistoryManagementRepository;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/history")
@CrossOrigin(origins = "*")
@RestController
public class HistoryManagementController {

    private final HistoryManagementRepository hitoryRepo;

    @GetMapping("/all")
    List<HistoryManagement> historyAll() {

        return hitoryRepo.findAll();

    }

}
