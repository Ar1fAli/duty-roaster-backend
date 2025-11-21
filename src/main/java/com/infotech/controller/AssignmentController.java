package com.infotech.controller;

import com.infotech.dto.AssignmentResponse;
import com.infotech.dto.GuardAssignmentRequest;
import com.infotech.service.AssignmentService;

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
@RequestMapping("/api/assignments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/auto")
    public ResponseEntity<AssignmentResponse> autoAssignGuards(
            @RequestBody GuardAssignmentRequest request) {

        AssignmentResponse response = assignmentService.assignGuardsAutomatically(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Read endpoint for frontend to fetch current assignments for a category
     * (user).
     * Frontend should call this on page load and call POST /auto only when assigned
     * < requested.
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<AssignmentResponse> getAssignmentsForCategory(@PathVariable Long categoryId) {
        AssignmentResponse response = assignmentService.getAssignmentResponseForCategory(categoryId);
        return ResponseEntity.ok(response);
    }
}
