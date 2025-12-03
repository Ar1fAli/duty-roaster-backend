package com.infotech.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.infotech.dto.Accidentreq;
import com.infotech.dto.LeaveReq;
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
        req.setRequestTime(LocalDateTime.now());

        return leaveRequestRepository.save(req);
    }

    @PostMapping("/decision/management")
    public LeaveRequest createCategorye(@RequestBody LeaveReq req) {

        System.out.println(req.getStatus());

        System.out.println("id is this " + req.getId());
        System.out.println("req is this " + req.getStatus());

        LeaveRequest acc = leaveRequestRepository.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("Accident not found with id: " + req.getId()));

        acc.setId(req.getId());
        acc.setStatus(req.getStatus());
        acc.setCurrent(true);
        acc.setResponseTime(LocalDateTime.now());

        return leaveRequestRepository.save(acc);
    }

    @PostMapping("/accident")
    public Accident createCategorye(@RequestBody Accident req) {
        System.out.println(req.getReq());
        System.out.println(req.getMessage());
        System.out.println(req.getGuardData());
        req.setRequestTime(LocalDateTime.now());

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

    @GetMapping("/accidentreq/{id}")
    public ResponseEntity<List<Accident>> getIncidient(@PathVariable Long id) {
        return ResponseEntity.ok(accidentRepository.findByGuardData_Id(id));
    }

    @GetMapping("/accidentall")
    public ResponseEntity<List<Accident>> getIncidient() {
        return ResponseEntity.ok(accidentRepository.findAll());
    }

    @PostMapping("/accidentupdate")
    public ResponseEntity<Accident> accidentUpdate(@RequestBody Accidentreq req) {

        System.out.println("id is this " + req.getId());
        System.out.println("req is this " + req.getReq());

        Accident acc = accidentRepository.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("Accident not found with id: " + req.getId()));

        acc.setId(req.getId());
        acc.setReq(req.getReq());
        acc.setResponseTime(LocalDateTime.now());
        accidentRepository.save(acc);

        return ResponseEntity.ok(acc);
    }

}
