package com.infotech.controller;

import java.util.List;

import com.infotech.dto.AssignmentResponse;
import com.infotech.dto.AssignmentResponsedto;
import com.infotech.dto.GuardAssignmentRequest;
import com.infotech.dto.GuardDutyHistorydto;
import com.infotech.dto.OfficerDuty;
import com.infotech.entity.UserGuardAssignment;
import com.infotech.service.AssignmentService;
import com.infotech.service.FcmService;

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

  private final FcmService service;

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
  public ResponseEntity<AssignmentResponsedto> getAssignmentsForCategory(@PathVariable Long categoryId) {
    AssignmentResponsedto response = assignmentService.getAssignmentResponseForCategory(categoryId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/getvip/{guardId}")
  public OfficerDuty getVipForGuard(@PathVariable Long guardId) {
    return assignmentService.getVipForGuard(guardId);
  }
  //
  // @PostMapping("/{assignmentId}/leave")
  // public AssignmentResponse markGuardOnLeave(
  // @PathVariable Long assignmentId,
  // @RequestBody GuardAssignmentRequest requirement) {
  //
  // return assignmentService.markGuardOnLeaveAndRefillit(assignmentId,
  // requirement);
  // }

  // @PostMapping("/refill")
  // public ResponseEntity<AssignmentResponse> refillMissingGuards(
  // @RequestParam("vipId") Long vipId,
  // @RequestParam("level") String level,
  // @RequestParam("missing") int missing) {
  //
  // AssignmentResponse response =
  // assignmentService.markGuardOnLeaveAndRefill(vipId, level, missing);
  // return ResponseEntity.ok(response);
  // }
  //
  // @PostMapping("/{vipId}/refill")
  // public AssignmentResponse refillGuards(
  // @PathVariable Long vipId,
  // @RequestBody GuardAssignmentRequest request) {
  //
  // return assignmentService.refillMissingGuards(vipId, request);
  // }
  //
  // @GetMapping("/getallguard")
  // public List<UserGuardAssignment> getHistory(@RequestBody Officer officer) {
  // return assignmentService.getHistory(officer);
  //
  // }
  //
  @GetMapping("/guard/{officerId}/history")
  public List<GuardDutyHistorydto> getGuardHistory(@PathVariable Long officerId) {
    // System.out.println(officerId);
    return assignmentService.getGuardHistory(officerId);
  }

  @GetMapping("/vip/{categoryId}/history")
  public UserGuardAssignment getVipHistory(@PathVariable Long categoryId) {
    // System.out.println(categoryId);
    return assignmentService.getVipHistory(categoryId);
  }

  @GetMapping("/getall")
  public List<UserGuardAssignment> getAllHistory() {

    return assignmentService.getAllHistory();

  }
}
//
// @PostMapping("/complete/vip/{categoryId}/{status}")
// public ResponseEntity<AssignmentResponse>
// completeDutyForCategory(@PathVariable Long categoryId,
// @PathVariable String status) {
// AssignmentResponse resp =
// assignmentService.completeDutyForCategory(categoryId, status);
// return ResponseEntity.ok(resp);
// }
//
// }
