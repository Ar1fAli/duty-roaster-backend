package com.infotech.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.infotech.dto.Accidentreq;
import com.infotech.dto.LeaveReq;
import com.infotech.entity.Accident;
import com.infotech.entity.LeaveRequest;
import com.infotech.entity.NotificationGuard;
import com.infotech.entity.NotificationManagement;
import com.infotech.entity.Officer;
import com.infotech.repository.AccidentRepository;
import com.infotech.repository.LeaveRequestRepository;
import com.infotech.repository.NotificationGuardRepository;
import com.infotech.repository.NotificationManagementRepository;
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
@RequestMapping("/api/duty")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DecisionController {

  private final LeaveRequestRepository leaveRequestRepository;
  private final AccidentRepository accidentRepository;
  private final AssignmentService assignmentService;
  private final NotificationGuardRepository notificationGuardRepository;
  private final NotificationManagementRepository notificationManagementRepo;
  private final FcmService service;

  @PostMapping("/decision")
  public LeaveRequest createCategorye(@RequestBody LeaveRequest req) {

    System.out.println(req.getStatus());
    System.out.println(req.getMessage());
    System.out.println(req.getOfficer());
    req.setCurrent(true);

    NotificationManagement notificationManagement = new NotificationManagement();

    notificationManagement.setNotificationSender("GUARD");
    notificationManagement.setNotificationSenderId(req.getOfficer().getId());
    notificationManagement.setNotificationMessage("Guard is Decided For Duty Please Check Status");
    notificationManagement.setNotificationStatus(false);
    notificationManagement.setNotificationSenderName(req.getOfficer().getName());

    notificationManagement.setNotificationAssignTime(LocalDateTime.now());

    notificationManagementRepo.save(notificationManagement);

    req.setRequestTime(LocalDateTime.now());

    return leaveRequestRepository.save(req);
  }

  @PostMapping("/decision/management")
  public ResponseEntity<LeaveRequest> decideLeave(@RequestBody LeaveReq req) {

    System.out.println("status is " + req.getStatus());
    System.out.println("id is " + req.getId());

    LeaveRequest leave = leaveRequestRepository.findById(req.getId())
        .orElseThrow(() -> new RuntimeException("LeaveRequest not found with id: " + req.getId()));

    // keep old status so we only trigger once
    String oldStatus = leave.getStatus();

    leave.setStatus(req.getStatus());
    leave.setCurrent(true);
    leave.setResponseTime(LocalDateTime.now());

    // accepted statuses
    boolean isNowAccepted = "admin_accepted".equalsIgnoreCase(req.getStatus())
        || "manager_accepted".equalsIgnoreCase(req.getStatus());

    // boolean wasAcceptedBefore = "admin_accepted".equalsIgnoreCase(oldStatus)
    // || "manager_accepted".equalsIgnoreCase(oldStatus);
    //
    // only trigger operation when changing from NOT accepted -> accepted
    if (isNowAccepted) {

      // 🔹 adjust this depending on your LeaveRequest entity:
      // e.g. leave.getOfficer() or leave.getGuardData()
      Officer officer = leave.getOfficer(); // <-- change to getGuardData() if needed

      if (officer == null || officer.getId() == null) {
        throw new RuntimeException("No guard linked to leave request id: " + req.getId());
      }

      Long officerId = officer.getId();

      // This will:
      // 1) mark the guard's last active assignment as Inactive
      // 2) set guard status = Inactive
      // 3) refill one guard for that VIP/category
      assignmentService.markGuardOnLeaveAndRefillByOfficer(officerId);
    }

    NotificationGuard notificationOfficer = new NotificationGuard();
    notificationOfficer.setRead(false);
    notificationOfficer.setOfficer(leave.getOfficer());
    notificationOfficer.setMessage("Your Request is Accepted by the Authority check the status of your request");
    notificationGuardRepository.save(notificationOfficer);

    leaveRequestRepository.save(leave);

    return ResponseEntity.ok(leave);
  }

  @PostMapping("/accident")
  public Accident createAccident(@RequestBody Accident req) {
    System.out.println(req.getReq());
    System.out.println(req.getMessage());
    System.out.println(req.getGuardData());

    req.setRequestTime(LocalDateTime.now());
    req.setRequestTime(LocalDateTime.now());

    NotificationManagement existingNotification = notificationManagementRepo
        .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(req.getGuardData().getId(),
            "guard");

    NotificationManagement existingNotificationUser = notificationManagementRepo
        .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(1L, "user");
    NotificationManagement existingNotificationAdmin = notificationManagementRepo
        .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(1L, "admin");
    // ✅ Get token from existing record
    String officerFcmToken = existingNotification != null
        ? existingNotification.getNotificationToken()
        : null;

    String userFcmToken = existingNotificationUser != null
        ? existingNotificationUser.getNotificationToken()
        : null;

    Long userId = existingNotificationUser != null
        ? existingNotificationUser.getNotificationSenderId()
        : null;

    String adminFcmToken = existingNotificationAdmin != null
        ? existingNotificationAdmin.getNotificationToken()
        : null;
    Long adminId = existingNotificationAdmin != null
        ? existingNotificationAdmin.getNotificationSenderId()
        : null;

    // ✅ Send notification immediately
    service.sendNotificationSafely(
        officerFcmToken,
        "Incidnet Occur",
        "Please Check The Incident From The Portal And Response it",
        "officer",
        req.getGuardData().getId());

    service.sendNotificationSafely(
        userFcmToken,
        "Incidnet Occur",
        "Please Check The Incident From The Portal And Response it",
        "Manager",
        userId);

    service.sendNotificationSafely(
        adminFcmToken,
        "Duty Assign",
        "Go And Check Your Duty From The Portal",
        "admin",
        adminId);

    // Create notification records (but use token from lookup)
    NotificationManagement notificationManagement = new NotificationManagement();
    notificationManagement.setNotificationSender("guard");
    notificationManagement.setNotificationSenderId(req.getGuardData().getId());
    notificationManagement.setNotificationMessage("Guard is Decided For Duty Please Check Status");
    notificationManagement.setNotificationStatus(false);
    notificationManagement.setNotificationSenderName(req.getGuardData().getName());
    notificationManagement.setNotificationAssignTime(LocalDateTime.now());
    notificationManagement.setNotificationToken(officerFcmToken); // Use the looked-up token
    notificationManagementRepo.save(notificationManagement);

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

  // @PostMapping("/accidentupdate")
  // public ResponseEntity<Accident> accidentUpdate(@RequestBody Accidentreq req)
  // {
  //
  // System.out.println("id is this " + req.getId());
  // System.out.println("req is this " + req.getReq());
  //
  // Accident acc = accidentRepository.findById(req.getId())
  // .orElseThrow(() -> new RuntimeException("Accident not found with id: " +
  // req.getId()));
  //
  // acc.setId(req.getId());
  // acc.setReq(req.getReq());
  // acc.setResponseTime(LocalDateTime.now());
  // accidentRepository.save(acc);
  //
  // return ResponseEntity.ok(acc);
  // }

  @PostMapping("/accidentupdate")
  public ResponseEntity<Accident> accidentUpdate(@RequestBody Accidentreq req) {

    System.out.println("id is this " + req.getId());
    System.out.println("req is this " + req.getReq());

    Accident acc = accidentRepository.findById(req.getId())
        .orElseThrow(() -> new RuntimeException(
            "Accident not found with id: " + req.getId()));

    // old status to detect first-time acceptance
    String oldStatus = acc.getReq();

    // update status + response time
    acc.setReq(req.getReq());
    acc.setResponseTime(LocalDateTime.now());

    // accepted statuses
    boolean isNowAccepted = "admin_rejected".equalsIgnoreCase(req.getReq())
        || "manager_rejected".equalsIgnoreCase(req.getReq());

    // boolean wasAcceptedBefore = "admin_accepted".equalsIgnoreCase(oldStatus)
    // || "manager_accepted".equalsIgnoreCase(oldStatus);
    //
    // only trigger when changing from NOT accepted -> accepted
    if (!isNowAccepted) {

      Officer guard = acc.getGuardData();
      if (guard == null || guard.getId() == null) {
        throw new RuntimeException(
            "No guard linked to accident id: " + req.getId());
      }

      Long officerId = guard.getId();

      // this will:
      // 1) mark guard's active assignment Inactive
      // 2) set guard status = Inactive
      // 3) refill one guard for that VIP/category
      assignmentService.markGuardOnLeaveAndRefillByOfficer(officerId);
    }

    NotificationGuard notificationOfficer = new NotificationGuard();
    notificationOfficer.setRead(false);
    notificationOfficer.setOfficer(acc.getGuardData());
    notificationOfficer.setMessage("Your Request is Accepted by the Authority check the status of your request");
    notificationGuardRepository.save(notificationOfficer);

    accidentRepository.save(acc);

    return ResponseEntity.ok(acc);
  }

}
