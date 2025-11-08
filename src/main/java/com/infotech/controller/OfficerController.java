package com.infotech.controller;

import java.util.List;

import com.infotech.entity.Officer;
import com.infotech.repository.OfficerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/officer")
// @CrossOrigin(origins = "http://localhost:5173")
@CrossOrigin(origins = "http://192.168.29.45:3000")
public class OfficerController {

    @Autowired
    private OfficerRepository officerRepository;

    @GetMapping
    public List<Officer> getAllOfficer() {
        return officerRepository.findAll();
    }

    @PostMapping
    public Officer createCategory(@RequestBody Officer officer) {
        return officerRepository.save(officer);
    }

    @PutMapping("/{id}")
    public Officer updateOfficer(@PathVariable Long id, @RequestBody Officer updatedOfficer) {
        return officerRepository.findById(id).map(officer -> {
            officer.setGuard_id(updatedOfficer.getGuard_id());
            officer.setGuard_name(updatedOfficer.getGuard_name());
            officer.setGuard_email(updatedOfficer.getGuard_email());
            officer.setGuard_rank(updatedOfficer.getGuard_rank());
            officer.setGuard_experience(updatedOfficer.getGuard_experience());
            officer.setContact_no(updatedOfficer.getContact_no());

            // Clear existing items (important for orphanRemoval = true)
            // officer.getOfficerName().clear();

            // Add updated items with proper category assignment
            // for (OfficerName item : updatedOfficer.getOfficerName()) {
            // item.setOfficer(officer);
            // officer.getOfficerName().add(item);
            // }

            return officerRepository.save(officer);
        }).orElseThrow(() -> new RuntimeException("Category not found with id " + id));
    }

    @DeleteMapping("/{id}")
    public void deleteOfficer(@PathVariable Long id) {
        officerRepository.deleteById(id);
    }
}
