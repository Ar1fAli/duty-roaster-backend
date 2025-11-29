package com.infotech.controller;

import java.util.List;

import com.infotech.entity.LeaveRequest;
import com.infotech.repository.LeaveRequestRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/duty")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DecisionController {

    private final LeaveRequestRepository leaveRequestRepository;

    @PostMapping("/decision")
    public LeaveRequest createCategorye(@RequestBody LeaveRequest req) {
        System.out.println(req.getStatus());
        System.out.println(req.getMessage());
        System.out.println(req.getOfficer());
        req.setCurrent(true);
        return leaveRequestRepository.save(req);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<LeaveRequest>> getCurrentLeave(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestRepository.findByOfficer_IdAndCurrent(id, true));
    }

}
