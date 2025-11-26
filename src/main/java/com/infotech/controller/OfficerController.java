package com.infotech.controller;

import java.util.List;
import java.util.Optional;

import com.infotech.entity.Officer;
import com.infotech.repository.OfficerRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
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
// @CrossOrigin(origins = "http://localhost:5173")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OfficerController {

    private final OfficerRepository officerRepository;
    private final PasswordEncoder encoder;

    @GetMapping
    public List<Officer> getAllOfficer() {
        return officerRepository.findAll();
    }

    @PostMapping
    public Officer createCategory(@RequestBody Officer officer) {

        officer.setPassword(encoder.encode(officer.getPassword()));
        return officerRepository.save(officer);
    }

    @GetMapping("/profile")
    public Optional<Officer> getAdmin(@RequestParam String username) {
        Optional<Officer> admindata = officerRepository.findByUsername(username);
        return admindata;
    }

    @PutMapping("/{id}")
    public Officer updateOfficer(@PathVariable Long id, @RequestBody Officer updatedOfficer) {
        return officerRepository.findById(id).map(officer -> {
            // officer.setGuard_id(updatedOfficer.getGuard_id());
            officer.setName(updatedOfficer.getName());
            officer.setEmail(updatedOfficer.getEmail());
            officer.setRank(updatedOfficer.getRank());
            officer.setStatus(updatedOfficer.getStatus());
            officer.setExperience(updatedOfficer.getExperience());
            officer.setContactno(updatedOfficer.getContactno());
            officer.setPassword(encoder.encode(updatedOfficer.getPassword()));

            officer.setUsername(updatedOfficer.getUsername());

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
