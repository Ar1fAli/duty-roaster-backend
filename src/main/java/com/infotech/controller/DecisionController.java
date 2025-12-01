package com.infotech.controller;

import java.util.List;

import com.infotech.dto.Accidentreq;
import com.infotech.entity.Accident;
import com.infotech.entity.LeaveRequest;
import com.infotech.repository.AccidentRepository;
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
    private final AccidentRepository accidentRepository;

    @PostMapping("/decision")
    public LeaveRequest createCategorye(@RequestBody LeaveRequest req) {
        System.out.println(req.getStatus());
        System.out.println(req.getMessage());
        System.out.println(req.getOfficer());
        req.setCurrent(true);
        return leaveRequestRepository.save(req);
    }

    @PostMapping("/accident")
    public Accident createCategorye(@RequestBody Accident req) {
        System.out.println(req.getReq());
        System.out.println(req.getMessage());
        System.out.println(req.getOfficer());
        return accidentRepository.save(req);
    }

    @GetMapping("/decision/all")
    public List<LeaveRequest> getAllLeaves() {
        return leaveRequestRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<LeaveRequest>> getCurrentLeave(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestRepository.findByOfficer_IdAndCurrent(id, true));
    }

    @PostMapping("/accidentreq")
    public ResponseEntity<List<Accident>> getIncidient(@RequestBody Accidentreq req) {
        return ResponseEntity.ok(accidentRepository.findByOfficer_IdAndReq(req.getId(), req.getReq()));
    }

}
