package com.infotech.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.infotech.dto.AssignmentHistoryDto;
import com.infotech.dto.AssignmentResponse;
import com.infotech.dto.AssignmentResponsedto;
import com.infotech.dto.AssignmentSummary;
import com.infotech.dto.CategoryDto;
import com.infotech.dto.GuardAssignmentRequest;
import com.infotech.dto.GuardDutyHistorydto;
import com.infotech.dto.GuardHistoryResponse;
import com.infotech.dto.GuardLevelRequest;
import com.infotech.dto.OfficerDto;
import com.infotech.dto.OfficerDuty;
import com.infotech.dto.ReplacedOfficerDto;
import com.infotech.entity.AdminEntity;
import com.infotech.entity.AssignmentHistoryEntity;
import com.infotech.entity.Category;
import com.infotech.entity.NotificationManagement;
import com.infotech.entity.Officer;
import com.infotech.entity.ReplacedOfficerEntity;
import com.infotech.entity.UserEntity;
import com.infotech.entity.UserGuardAssignment;
import com.infotech.exception.GuardAssignmentException;
import com.infotech.repository.AdminRepsitory;
import com.infotech.repository.AssignmentHistoryRepository;
import com.infotech.repository.CategoryRepository;
import com.infotech.repository.NotificationCategoryRepository;
import com.infotech.repository.NotificationGuardRepository;
import com.infotech.repository.NotificationManagementRepository;
import com.infotech.repository.OfficerRepository;
import com.infotech.repository.ReplacedOfficerRepository;
import com.infotech.repository.TaskRepository;
import com.infotech.repository.UserGuardAssignmentRepository;
import com.infotech.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {

  private final CategoryRepository categoryRepository;
  private final OfficerRepository officerRepository;
  private final UserGuardAssignmentRepository assignmentRepository;
  private final NotificationCategoryRepository notificationCategoryRepository;
  private final NotificationGuardRepository notificationGuardRepository;
  private final NotificationManagementRepository notificationManagementRepo;
  private final ReplacedOfficerRepository replacedOfficerRepository;
  private final UserRepository userRepository;
  private final AdminRepsitory adminRepository;
  private final TaskRepository taskRepository;
  private final AssignmentHistoryRepository assignmentHistoryRepository;

  private final FcmService service;

  @PersistenceContext
  private EntityManager em;

  private static final int MAX_ASSIGN_PER_VIP = 4;

  @Transactional
  public AssignmentResponse assignGuardsAutomatically(GuardAssignmentRequest request) {

    Category category = categoryRepository.findById(request.getUserId())
        .orElseThrow(() -> new RuntimeException("VIP not found"));

    List<AssignmentSummary> summaries = new ArrayList<>();
    Map<String, List<Officer>> finalSelections = new LinkedHashMap<>();
    List<Officer> allSelectedOfficers = new ArrayList<>();

    // 🔒 PHASE 1: VALIDATION ONLY (NO DB WRITE)
    for (GuardLevelRequest levelReq : request.getLevels()) {

      String level = levelReq.getGuardLevel();
      int requested = levelReq.getNumberOfGuards();

      // 1️⃣ History for this VIP + rank
      List<UserGuardAssignment> history = assignmentRepository.findByCategoryAndOfficerRank(category, level);

      Map<Long, Long> usageCount = history.stream()
          .flatMap(a -> a.getOfficers().stream())
          .collect(Collectors.groupingBy(
              Officer::getId,
              Collectors.counting()));

      // 2️⃣ All inactive officers of rank
      List<Officer> inactive = officerRepository.findByRank(level)
          .stream()
          .filter(o -> "Inactive".equalsIgnoreCase(o.getStatus()))
          .collect(Collectors.toList());

      // 3️⃣ Eligible officers (below limit)
      List<Officer> eligible = inactive.stream()
          .filter(o -> usageCount.getOrDefault(o.getId(), 0L) < MAX_ASSIGN_PER_VIP)
          .collect(Collectors.toList());

      int blockedByLimit = inactive.size() - eligible.size();

      // 🚨 HARD FAIL — REQUIREMENT NOT MET
      if (eligible.size() < requested) {

        String reason;

        if (inactive.isEmpty()) {
          reason = "No inactive officers available for rank '" + level + "'";
        } else if (eligible.isEmpty()) {
          reason = "All inactive officers of rank '" + level +
              "' reached assignment limit (" + MAX_ASSIGN_PER_VIP + ")";
        } else {
          reason = "Only " + eligible.size() + " eligible officers available, " +
              "but " + requested + " required. " +
              blockedByLimit + " officers blocked by limit";
        }

        throw new GuardAssignmentException(
            "Assignment FAILED for rank '" + level + "'. Reason: " + reason);
      }

      // 4️⃣ Select officers (safe now)
      Collections.shuffle(eligible);
      List<Officer> selected = eligible.subList(0, requested);

      finalSelections.put(level, selected);
      allSelectedOfficers.addAll(selected);

      summaries.add(new AssignmentSummary(
          level,
          requested,
          requested,
          0,
          blockedByLimit,
          inactive.size()));
    }

    // 🔒 PHASE 2: SAFE TO COMMIT (ALL LEVELS PASSED)

    UserGuardAssignment assignment = new UserGuardAssignment();
    assignment.setCategory(category);
    assignment.setOfficers(allSelectedOfficers);
    assignment.setStatus("Active Duty");
    assignment.setAssignedAt(LocalDateTime.now());
    assignment.setStartAt(request.getStartAt());
    assignment.setEndAt(request.getEndAt());

    UserGuardAssignment savedAssignment = assignmentRepository.save(assignment);

    for (Officer officer : allSelectedOfficers) {
      officer.setStatus("Active");

      if (officer.getAssignments() == null) {
        officer.setAssignments(new ArrayList<>());
      }
      officer.getAssignments().add(savedAssignment);

      officerRepository.save(officer);
    }

    category.setStatus("Active");
    if (category.getAssignments() == null) {
      category.setAssignments(new ArrayList<>());
    }
    category.getAssignments().add(savedAssignment);
    categoryRepository.save(category);

    AssignmentResponse response = new AssignmentResponse();
    response.setSummary(summaries);
    response.setDetails(List.of(savedAssignment));

    return response;
  }

  // -----------------------------------------------
  // assignGuardsAutomatically (unchanged)
  // -----------------------------------------------
  @Transactional
  public AssignmentResponse assignGuardsAutomatically2(GuardAssignmentRequest request) {

    Category category = categoryRepository.findById(request.getUserId())
        .orElseThrow(() -> new RuntimeException(
            "Category (user) not found"));

    List<UserGuardAssignment> responseDetails = new ArrayList<>();
    List<AssignmentSummary> summaries = new ArrayList<>();
    UserGuardAssignment assignedassigment = new UserGuardAssignment();
    List<Officer> assignedOfficers = new ArrayList<>();

    Map<String, List<Officer>> selectionsByLevel = new LinkedHashMap<>();
    Map<String, Integer> requestedByLevel = new HashMap<>();

    for (GuardLevelRequest levelReq : request.getLevels()) {
      String level = levelReq.getGuardLevel();
      int requestedCount = levelReq.getNumberOfGuards();
      requestedByLevel.put(level, requestedCount);

      // List<UserGuardAssignment> activeForLevel = assignmentRepository
      // .findByCategoryAndGuardLevelAndStatus(
      // category,
      // level,
      // "Active");
      //
      //
      // if (activeCount >= requestedCount) {
      // summaries.add(new AssignmentSummary(level,
      // requestedCount,
      // activeCount,
      // 0));
      // selectionsByLevel.put(level, Collections.emptyList());
      // continue;
      // }
      //
      // int missing = requestedCount - activeCount;
      //
      // List<UserGuardAssignment> previous = assignmentRepository
      // .findByCategoryAndGuardLevel(category,
      // level);
      //
      // Set<Long> activeGuardIds = previous.stream()
      // .filter(a -> "Active".equalsIgnoreCase(a
      // .getStatus()))
      // .map(a -> a.getOfficer().getId())
      // .collect(Collectors.toSet());
      //
      // List<UserGuardAssignment> inactivePrevious = previous.stream()
      // .filter(a -> !"Active".equalsIgnoreCase(
      // a.getStatus()))
      // .collect(Collectors.toList());
      //
      List<Officer> allInactiveThisLevel = officerRepository.findByRank(
          level)
          .stream()
          .filter(o -> "Inactive".equalsIgnoreCase(
              o.getStatus()))
          .collect(Collectors.toList());
      int activeCount = allInactiveThisLevel.size();

      List<Officer> combinedPool = new ArrayList<>(allInactiveThisLevel);
      Collections.shuffle(combinedPool);

      int willAssign = Math.min(requestedCount, combinedPool.size());

      List<Officer> selected = combinedPool.subList(0, willAssign);

      // int willAssign = Math.min(missing, combinedPool.size());
      // List<Officer> selected = combinedPool.subList(0, requestedCount);

      // List<Officer> availableGuards = allInactiveThisLevel.stream()
      // .filter(o -> !activeGuardIds.contains(o
      // .getId()))
      // .collect(Collectors.toList());
      //
      // Map<Long, Integer> timesMap = inactivePrevious.stream()
      // .collect(Collectors.toMap(
      // a -> a.getOfficer().getId(),
      // UserGuardAssignment::getTimesAssigned,
      // Math::max));
      //
      // Set<Long> excluded = timesMap.entrySet()
      // .stream()
      // .filter(e -> e.getValue() >= MAX_REUSE)
      // .map(Map.Entry::getKey)
      // .collect(Collectors.toSet());
      //
      // List<Officer> cleaned = availableGuards.stream()
      // .filter(o -> !excluded.contains(o
      // .getId()))
      // .collect(Collectors.toList());
      //
      // List<Officer> reusable = inactivePrevious.stream()
      // .filter(a -> a.getTimesAssigned() < MAX_REUSE)
      // .map(UserGuardAssignment::getOfficer)
      // .filter(o -> !excluded.contains(o
      // .getId()))
      // .collect(Collectors.toList());
      //
      // Map<Long, Officer> combined = new LinkedHashMap<>();
      // cleaned.forEach(o -> combined.put(o.getId(), o));
      // reusable.forEach(o -> combined.putIfAbsent(o.getId(), o));
      //
      // for (Officer o : selected) {
      // assignedOfficers.add(o);
      // assignedassigment = new UserGuardAssignment();
      // assignedassigment.setCategory(category);
      // assignedassigment.setGuardLevel(level);
      // assignedassigment.setOfficer(o);
      // assignedassigment.setStatus("Active");
      // responseDetails.add(assignedassigment);
      // }

      assignedOfficers.addAll(selected);

      selectionsByLevel.put(level, selected);

      // int totalAssignedNow = activeCount + selected.size();
      summaries.add(new AssignmentSummary(level, requestedCount,
          selected.size(), 4, 3, Math.max(0, requestedCount - selected.size())));
    }
    assignedassigment.setStatus("Active Duty");
    assignedassigment.setAssignedAt(LocalDateTime.now());
    assignedassigment.setStartAt(request.getStartAt());
    assignedassigment.setEndAt(request.getEndAt());
    assignedassigment.setCategory(category);

    assignedassigment.setOfficers(assignedOfficers);

    List<String> failLevels = summaries.stream()
        .filter(s -> s.getMissing() > 0)
        .map(AssignmentSummary::getLevel)
        .collect(Collectors.toList());

    if (assignedOfficers.isEmpty()) {
      throw new RuntimeException("No guards available for assignment");
    }

    UserGuardAssignment savedAssignments = assignmentRepository.save(assignedassigment);
    for (Map.Entry<String, List<Officer>> entry : selectionsByLevel.entrySet()) {
      String level = entry.getKey();
      List<Officer> selected = entry.getValue();
      //
      // if (selected.isEmpty()) {
      // List<UserGuardAssignment> activeOld = assignmentRepository
      // .findByCategoryAndGuardLevelAndStatus(
      // category,
      // level,
      // "Active");
      // responseDetails.addAll(activeOld);
      // continue;
      // }

      // List<UserGuardAssignment> previous = assignmentRepository
      // .findByCategoryAndGuardLevel(category,
      // level);
      //
      // List<UserGuardAssignment> inactivePrev = previous.stream()
      // .filter(a -> !"Active".equals(a.getStatus()))
      // .collect(Collectors.toList());
      //
      // List<UserGuardAssignment> created = new ArrayList<>();

      for (Officer officer : selected) {
        officer.setStatus("Active");

        if (officer.getAssignments() == null) {
          officer.setAssignments(new ArrayList<>());
        }
        officer.getAssignments().add(savedAssignments);
        // officer.getAssignments().add(savedAssignments);
        officerRepository.save(officer);

        // ✅ CORRECT: Look up token BEFORE creating new notification
        NotificationManagement existingNotification = notificationManagementRepo
            .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(officer.getId(),
                "guard");

        // ✅ Get token from existing record
        String officerFcmToken = existingNotification != null
            ? existingNotification.getNotificationToken()
            : null;

        // ✅ Send notification immediately
        service.sendNotificationSafely(
            officerFcmToken,
            "Duty Assign",
            "Go And Check Your Duty From The Portal",
            "officer",
            officer.getId());

        // Optional<UserGuardAssignment> exist = inactivePrev
        // .stream()
        // .filter(a -> a.getOfficer().getId()
        // .equals(officer.getId()))
        // .findFirst();
        //
        // Create notification records (but use token from lookup)
        NotificationManagement notification = new NotificationManagement();
        // NotificationGuard notificationGuard = new NotificationGuard();
        // notificationGuard.setRead(false);
        // notificationGuard.setOfficer(officer);
        // notificationGuard.setMessage("New Duty Assign Please Check Your Duty");

        notification.setNotificationSenderId(officer.getId());
        notification.setNotificationSender("guard");
        notification.setNotificationSenderName(officer.getName());
        notification.setNotificationMessage("New Duty Assign Please Check Your  Duty");
        notification.setNotificationToken(officerFcmToken); // Use the looked-up token
        notification.setNotificationStatus(false);
        notification.setNotificationAssignTime(LocalDateTime.now());

        notificationManagementRepo.save(notification);
        // notificationGuardRepository.save(notificationGuard);

        // UserGuardAssignment a;
        // if (exist.isPresent()) {
        // a = exist.get();
        // a.setTimesAssigned(a.getTimesAssigned() + 1);
        // } else {
        // a = new UserGuardAssignment();
        // a.setOfficer(officer);
        // a.setTimesAssigned(1);
      }

      // created.add(assignedassigment);
    }

    if (assignedassigment != null) {

      // 1. Save UserGuardAssignments first

      responseDetails.add(savedAssignments);

      // 2. Create TaskEntity for each assignment
      // TaskEntity tasks = new TaskEntity();

      // tasks.setUserGuardAssignment(savedAssignments);
      // 3. Save all tasks
      // taskRepository.save(tasks);
      if (!responseDetails.isEmpty()) {
        category.setStatus("Active");
        // category.setUserGuardAssignment(savedAssignments);
        category.getAssignments().add(savedAssignments);

        categoryRepository.save(category);
      }
    }

    // responseDetails.addAll(assignmentRepository
    // .findByCategoryAndGuardLevelAndStatus(
    // category,
    // level,
    // "Active"));
    // }

    // BEFORE creating new notification, look up existing token
    NotificationManagement existingVipNotification = notificationManagementRepo
        .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(category.getId(), "vip");

    String vipFcmToken = existingVipNotification != null
        ? existingVipNotification.getNotificationToken()
        : null;

    // Send notification with existing token
    service.sendNotificationSafely(
        vipFcmToken,
        "Duty Assign",
        "For More Detail Check It From The Portal",
        "category",
        category.getId());

    // THEN create new notification record
    NotificationManagement notificationcat = new NotificationManagement();
    notificationcat.setNotificationSenderId(category.getId());
    notificationcat.setNotificationSender("vip");
    notificationcat.setNotificationSenderName(category.getName());
    notificationcat.setNotificationMessage("Duty Assign Successfully");
    notificationcat.setNotificationToken(vipFcmToken); // Use looked-up token
    notificationcat.setNotificationStatus(false);
    notificationcat.setNotificationAssignTime(LocalDateTime.now());

    notificationManagementRepo.save(notificationcat);

    AdminEntity admin = adminRepository.findById(1L)
        .orElseThrow(() -> new RuntimeException("Admin not found"));
    // AdminEntity admin = adminRepository.findById(1L).orElse(null);
    NotificationManagement notificationadmin = notificationManagementRepo
        .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(admin.getId(), "admin");

    String adminFcmToken = notificationadmin != null
        ? notificationadmin.getNotificationToken()
        : null;
    NotificationManagement notificationtoadmin = new NotificationManagement();
    notificationtoadmin.setNotificationSenderId(admin.getId());
    notificationtoadmin.setNotificationSender("admin");
    notificationtoadmin.setNotificationSenderName(admin.getAdminName());
    notificationtoadmin
        .setNotificationMessage("Duty Assign Successfully For The Vip And Name Is ==>  " + category.getName());
    if (notificationadmin != null) {
      notificationtoadmin.setNotificationToken(adminFcmToken); // Use looked-up token
    }
    notificationtoadmin.setNotificationStatus(false);
    notificationtoadmin.setNotificationAssignTime(LocalDateTime.now());

    notificationManagementRepo.save(notificationtoadmin);

    AssignmentResponse resp = new AssignmentResponse();
    resp.setSummary(summaries);
    resp.setDetails(responseDetails);

    return resp;
  } // -----------------------------------------------
  // getAssignmentResponseForCategory (unchanged)
  // -----------------------------------------------
  // inside AssignmentService

  private static final String ACTIVE_STATUS = "Active Duty";

  //
  @Transactional()
  public AssignmentResponsedto getAssignmentResponseForCategory(Long categoryId) {

    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new RuntimeException(
            "Category not found for id: " + categoryId));

    // Load all assignments for this category, officer is
    // fetched via @EntityGraph
    // List<UserGuardAssignment> all = assignmentRepository.findByCategoryId(
    // categoryId);
    // System.out.println(category.getUserGuardAssignment().getId() + "category
    // values");
    //
    //

    UserGuardAssignment assignment = category.getAssignments().stream().filter(a -> ACTIVE_STATUS.equals(a.getStatus()))
        .findFirst().get();
    AssignmentResponsedto assignmentresponse = new AssignmentResponsedto();

    if (assignment != null) {

      System.out.println(assignment.getId() + "assigment values");
      assignmentresponse.setId(assignment.getId());
      assignmentresponse.setEndAt(assignment.getEndAt());
      assignmentresponse.setAtEnd(assignment.getAtEnd());
      assignmentresponse.setStatus(assignment.getStatus());
      assignmentresponse.setOfficer(assignment.getOfficers());
    }

    if (category != null) {

      assignmentresponse.setCategory(assignment.getCategory());
    }
    assignmentresponse.setAssignedAt(assignment.getAssignedAt());
    assignmentresponse.setStartAt(assignment.getStartAt());

    return assignmentresponse;

    // Keep only active-duty rows
    // List<UserGuardAssignment> active = Optional.ofNullable(all)
    // .orElse(Collections.emptyList())
    // .stream()
    // .filter(a -> ACTIVE_STATUS.equalsIgnoreCase(
    // a.getStatus()))
    // .collect(Collectors.toList());
    //
    // Group by rank for summary
    // Map<String, Long> byLevel = active.stream()
    // .collect(Collectors.groupingBy(
    // a -> a.getOfficer() != null
    // ? a.getOfficer().getRank()
    // : "UNASSIGNED",
    // Collectors.counting()));
    //
    // List<AssignmentSummary> summaries = byLevel.entrySet().stream()
    // .map(e -> new AssignmentSummary(
    // e.getKey(),
    // e.getValue().intValue(), // requested
    // // (or
    // // 0
    // // if
    // // you
    // // prefer)
    // e.getValue().intValue(), // assigned
    // 0 // missing
    // ))
    // .collect(Collectors.toList());
    //
    // AssignmentResponse resp = new AssignmentResponse();
    // resp.setSummary(summaries);
    //
    // // 🔧 FIX: use the SAME status here as above
    // resp.setDetails(active);
    //
    // return resp;
  }

  //
  // // -----------------------------------------------
  // // getVipForGuard (unchanged)
  // // -----------------------------------------------
  @Transactional(readOnly = true)
  public OfficerDuty getVipForGuard(Long officerId) {
    OfficerDuty dto = new OfficerDuty();

    Officer officer = officerRepository.findById(officerId)
        .orElseThrow(() -> new RuntimeException("Officer not found: " + officerId));

    UserGuardAssignment assignment = officer.getAssignments().stream().filter(a -> ACTIVE_STATUS.equals(a.getStatus()))
        .findFirst().get();
    if (assignment == null) {
      throw new RuntimeException("No active VIP assignment for guard " + officerId);
    }

    // if you really need to re-fetch from DB
    UserGuardAssignment persistedAssignment = assignmentRepository.findById(assignment.getId())
        .orElseThrow(() -> new RuntimeException(
            "Assignment not found for id " + assignment.getId()));

    // Long assignmentId =
    // officerRepository.findById(officerId).get().getUserGuardAssignment().getId();
    // if (assignmentId == null) {
    // throw new RuntimeException("No active VIP assignment for guard " +
    // officerId);
    // }else{
    // UserGuardAssignment assignment = assignmentRepository.findById(assignmentId);
    //
    // }
    //
    // assignmentRepository
    // .findFirstByOfficerIdAndStatusOrderByAssignedAtDesc(
    // officerId,
    // "Active Duty")
    // .orElseThrow(() -> new RuntimeException(
    // "No active VIP assignment for guard "
    // + officerId));

    Category cat = persistedAssignment.getCategory();

    dto.setName(cat.getName());
    dto.setDesignation(cat.getDesignation());
    dto.setStartAt(persistedAssignment.getStartAt());
    dto.setEndAt(persistedAssignment.getEndAt());
    dto.setId(persistedAssignment.getId());

    return dto;
  }

  // // ============================================================
  // // ✔ FIXED: markGuardOnLeaveAndRefill
  // // ============================================================
  // @Transactional
  // public AssignmentResponse markGuardOnLeaveAndRefillit(Long assignmentId,
  // GuardAssignmentRequest requirement) {
  //
  // UserGuardAssignment assignment = assignmentRepository.findById(assignmentId)
  // .orElseThrow(() -> new RuntimeException(
  // "Assignment not found"));
  //
  // // Only THIS assignment becomes Inactive
  // assignment.setStatus("Inactive");
  // assignmentRepository.save(assignment);
  //
  // // Guard itself becomes inactive
  // Officer officer = assignment.getOfficer();
  // officer.setStatus("Inactive");
  // officerRepository.save(officer);
  //
  // Long vipId = assignment.getCategory().getId();
  //
  // NotificationManagement notificationManagement = new NotificationManagement();
  // NotificationManagement notificationmange = notificationManagementRepo
  // .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(vipId,
  // "vip");
  //
  // notificationManagement.setNotificationSender("vip");
  // notificationManagement.setNotificationSenderId(vipId);
  // notificationManagement.setNotificationMessage("Duty Completed");
  // notificationManagement.setNotificationStatus(false);
  // notificationManagement.setNotificationSenderName(assignment.getCategory()
  // .getName());
  //
  // notificationManagement.setNotificationAssignTime(LocalDateTime.now());
  //
  // if (notificationmange != null) {
  // notificationManagement.setNotificationToken(notificationmange.getNotificationToken());
  // }
  //
  // // service.send(notificationman.getNotificationToken(), "Duty Assign",
  // "ForMore
  // // Detail Check It From The Portal");
  // if (notificationmange != null) {
  // try {
  // service.send(
  // notificationmange.getNotificationToken(),
  // "Duty Assign",
  // "For More Detail Check It From The Portal");
  // } catch (Exception e) {
  // log.error("FCM send failed for vip {}", vipId, e);
  // }
  // }
  //
  // notificationManagementRepo.save(notificationManagement);
  //
  // return refillMissingGuards(vipId, requirement);
  // }

  // @Transactional
  // public AssignmentResponse markGuardOnLeaveAndRefill(Long vipId, String level,
  // int missing) {
  //
  // Category category = categoryRepository.findById(vipId)
  // .orElseThrow(() -> new RuntimeException(
  // "Category not found"));
  //
  // List<UserGuardAssignment> responseDetails = new ArrayList<>();
  // List<AssignmentSummary> summaries = new ArrayList<>();
  //
  // // Get currently active
  // List<UserGuardAssignment> active = assignmentRepository
  // .findByCategoryAndGuardLevelAndStatus(category, level,
  // "Active");
  //
  // List<UserGuardAssignment> inactive = assignmentRepository
  // .findByCategoryAndGuardLevelAndStatus(category, level,
  // "Inactive");
  //
  // active.forEach(a -> {
  // Officer officer = a.getOfficer();
  // System.out.print("Name " + officer.getName());
  // System.out.print("Id " + officer.getId());
  // System.out.print("Status " + officer.getStatus());
  // System.out.print("Rank " + officer.getRank());
  // });
  // System.out.println();
  // inactive.forEach(a -> {
  // Officer officer = a.getOfficer();
  // System.out.print("Name " + officer.getName());
  // System.out.print("Id " + officer.getId());
  // System.out.print("Status " + officer.getStatus());
  // System.out.print("Rank " + officer.getRank());
  // });
  //
  // int activeCount = active.size();
  //
  // // If nothing is actually missing, just return summary +
  // // current actives
  // ReplacedOfficerEntity replaced = replacedOfficerRepository
  // .findFirstByCategoryAndStatusOrderByIdDesc(category, "Pending")
  // .orElseThrow(() -> new RuntimeException("No Pending Replacements"));
  // if (missing <= 0) {
  // summaries.add(new AssignmentSummary(
  // level,
  // activeCount + missing, // requested
  // // total
  // activeCount, // actually
  // // active
  // 0 // shortfall
  // ));
  // responseDetails.addAll(active);
  // replaced.setStatus("Completed");
  // replacedOfficerRepository.save(replaced);
  // return new AssignmentResponse(summaries, responseDetails);
  // }
  //
  // // Previous assignments (any status)
  // List<UserGuardAssignment> previous = assignmentRepository
  // .findByCategoryAndGuardLevel(category, level);
  //
  // Set<Long> activeIds = previous.stream()
  // .filter(a -> "Active".equals(a.getStatus()))
  // .map(a -> a.getOfficer().getId())
  // .collect(Collectors.toSet());
  //
  // // Inactive officers of this rank
  // List<Officer> inactiveOfficers = officerRepository.findByRank(level)
  // .stream()
  // .filter(o -> "Inactive".equals(o.getStatus()))
  // .collect(Collectors.toList());
  //
  // // Available officers (not already active)
  // List<Officer> available = inactiveOfficers.stream()
  // .filter(o -> !activeIds.contains(o.getId()))
  // .collect(Collectors.toList());
  //
  // // available.remove(replaced.getPreviousOfficer());
  // //
  // // if (available.isEmpty()) {
  // // throw new RuntimeException("No Officers Available");
  // // }
  // //
  // //
  // // System.out.println("previous " + previous);
  // // System.out.println();
  // // System.out.println("activeIds" + activeIds);
  // // System.out.println();
  // // System.out.println("inactiveOfficers" + inactiveOfficers);
  // // System.out.println();
  // // System.out.println("available" + available);
  // //
  // // RANDOM selection
  // Collections.shuffle(available);
  // List<Officer> selected = available.subList(0, Math.min(missing, available
  // .size()));
  //
  // List<UserGuardAssignment> created = new ArrayList<>();
  // for (Officer officer : selected) {
  //
  // officer.setStatus("Active");
  // officerRepository.save(officer);
  //
  // UserGuardAssignment newAssign = new UserGuardAssignment();
  // newAssign.setCategory(category);
  // newAssign.setOfficer(officer);
  // newAssign.setStatus("Active Duty");
  // newAssign.setAssignedAt(LocalDateTime.now());
  //
  // // Reuse timing from old
  // // active assignment
  // if (!active.isEmpty()) {
  // newAssign.setStartAt(active.get(0).getStartAt());
  // newAssign.setEndAt(active.get(0).getEndAt());
  // }
  //
  // replaced.setCurrentOfficer(officer);
  //
  // created.add(newAssign);
  // }
  //
  // if (!created.isEmpty()) {
  // responseDetails.addAll(assignmentRepository.saveAll(created));
  // }
  //
  // // Get final list of active
  // List<UserGuardAssignment> nowActive = assignmentRepository
  // .findByCategoryAndGuardLevelAndStatus(category, level,
  // "Active");
  //
  // summaries.add(new AssignmentSummary(
  // level,
  // activeCount + missing, // required
  // // total
  // nowActive.size(), // actually
  // // active
  // // now
  // Math.max(0, (activeCount + missing) - nowActive.size()) // still
  // // short
  // ));
  //
  // replaced.setStatus("Completed");
  // replacedOfficerRepository.save(replaced);
  // responseDetails.addAll(nowActive);
  //
  // AssignmentResponse resp = new AssignmentResponse();
  // resp.setSummary(summaries);
  // resp.setDetails(responseDetails);
  //
  // return resp;
  // }
  //
  // // ============================================================
  // // ✔ FIXED: refillMissingGuards (raw entity list, no DTOs)
  // // ============================================================
  // @Transactional
  // public AssignmentResponse refillMissingGuards(Long vipId,
  // GuardAssignmentRequest requirement) {
  //
  // Category category = categoryRepository.findById(vipId)
  // .orElseThrow(() -> new RuntimeException(
  // "Category not found"));
  //
  // List<UserGuardAssignment> responseDetails = new ArrayList<>();
  // List<AssignmentSummary> summaries = new ArrayList<>();
  //
  // for (GuardLevelRequest levelReq : requirement.getLevels()) {
  //
  // String level = levelReq.getGuardLevel();
  // int requestedCount = levelReq.getNumberOfGuards();
  //
  // List<UserGuardAssignment> active = assignmentRepository
  // .findByCategoryAndGuardLevelAndStatus(
  // category,
  // level,
  // "Active");
  //
  // int activeCount = active.size();
  // int missing = requestedCount - activeCount;
  //
  // if (missing <= 0) {
  // summaries.add(new AssignmentSummary(level,
  // requestedCount,
  // activeCount,
  // 0));
  // responseDetails.addAll(active);
  // continue;
  // }
  //
  // List<UserGuardAssignment> previous = assignmentRepository
  // .findByCategoryAndGuardLevel(category,
  // level);
  //
  // Set<Long> activeGuardIds = previous.stream()
  // .filter(a -> "Active".equals(a.getStatus()))
  // .map(a -> a.getOfficer().getId())
  // .collect(Collectors.toSet());
  //
  // List<UserGuardAssignment> inactivePrev = previous.stream()
  // .filter(a -> !"Active".equals(a.getStatus()))
  // .collect(Collectors.toList());
  //
  // List<Officer> inactiveOfficers = officerRepository.findByRank(level)
  // .stream()
  // .filter(o -> "Inactive".equals(o.getStatus()))
  // .collect(Collectors.toList());
  //
  // List<Officer> available = inactiveOfficers.stream()
  // .filter(o -> !activeGuardIds.contains(o
  // .getId()))
  // .collect(Collectors.toList());
  //
  // Map<Long, Integer> timesMap = inactivePrev.stream()
  // .collect(Collectors.toMap(
  // a -> a.getOfficer().getId(),
  // UserGuardAssignment::getTimesAssigned,
  // Math::max));
  //
  // Set<Long> excluded = timesMap.entrySet().stream()
  // .filter(e -> e.getValue() >= MAX_REUSE)
  // .map(Map.Entry::getKey)
  // .collect(Collectors.toSet());
  //
  // List<Officer> cleaned = available.stream()
  // .filter(o -> !excluded.contains(o
  // .getId()))
  // .collect(Collectors.toList());
  //
  // List<Officer> reusable = inactivePrev.stream()
  // .filter(a -> a.getTimesAssigned() < MAX_REUSE)
  // .map(UserGuardAssignment::getOfficer)
  // .filter(o -> !excluded.contains(o
  // .getId()))
  // .collect(Collectors.toList());
  //
  // Map<Long, Officer> combined = new LinkedHashMap<>();
  // cleaned.forEach(o -> combined.put(o.getId(), o));
  // reusable.forEach(o -> combined.putIfAbsent(o.getId(), o));
  //
  // List<Officer> pool = new ArrayList<>(combined.values());
  // Collections.shuffle(pool);
  //
  // int willAssign = Math.min(missing, pool.size());
  // List<Officer> selected = pool.subList(0, willAssign);
  //
  // List<UserGuardAssignment> created = new ArrayList<>();
  //
  // for (Officer officer : selected) {
  // officer.setStatus("Active");
  // officerRepository.save(officer);
  //
  // Optional<UserGuardAssignment> exist = inactivePrev
  // .stream()
  // .filter(a -> a.getOfficer().getId()
  // .equals(officer.getId()))
  // .findFirst();
  //
  // UserGuardAssignment a;
  // if (exist.isPresent()) {
  // a = exist.get();
  // a.setTimesAssigned(a.getTimesAssigned()
  // + 1);
  // } else {
  // a = new UserGuardAssignment();
  // a.setCategory(category);
  // a.setOfficer(officer);
  // a.setTimesAssigned(1);
  // }
  //
  // a.setStatus("Active");
  // a.setAssignedAt(LocalDateTime.now());
  //
  // if (!active.isEmpty()) {
  // a.setStartAt(active.get(0).getStartAt());
  // a.setEndAt(active.get(0).getEndAt());
  // }
  //
  // created.add(a);
  // }
  //
  // if (!created.isEmpty()) {
  // responseDetails.addAll(assignmentRepository
  // .saveAll(created));
  // }
  //
  // List<UserGuardAssignment> nowActive = assignmentRepository
  // .findByCategoryAndGuardLevelAndStatus(
  // category,
  // level,
  // "Active");
  //
  // summaries.add(new AssignmentSummary(level,
  // requestedCount,
  // nowActive.size(),
  // Math.max(0, requestedCount - nowActive
  // .size())));
  //
  // responseDetails.addAll(nowActive);
  // }
  //
  // category.setStatus("Active");
  // categoryRepository.save(category);
  //
  // AssignmentResponse resp = new AssignmentResponse();
  // resp.setSummary(summaries);
  // resp.setDetails(responseDetails);
  //
  // return resp;
  // }
  //
  // @Transactional(readOnly = true)
  // public List<UserGuardAssignment> getHistory(Officer officer) {
  //
  // List<UserGuardAssignment> assignment = assignmentRepository
  // .findByOfficer(officer);
  // return assignment;
  // }
  //
  //
  //

  @Transactional(readOnly = true)
  public List<GuardDutyHistorydto> getGuardHistory(Long officerId) {

    officerRepository.findById(officerId)
        .orElseThrow(() -> new RuntimeException("Officer not found: " + officerId));

    return assignmentHistoryRepository.findAll().stream()
        .filter(a -> a.getOfficer() != null &&
            a.getOfficer().stream()
                .anyMatch(o -> o.getId().equals(officerId)))
        .map(a -> {
          GuardDutyHistorydto dto = new GuardDutyHistorydto();
          dto.setAssignmentId(a.getId());
          dto.setVipname(a.getCategory().stream().findFirst().get().getName());
          dto.setDesignation(a.getCategory().stream().findFirst().get().getDesignation());
          dto.setStartTime(a.getStartAt());
          dto.setEndTime(a.getEndAt());
          dto.setStatus(a.getStatus());
          return dto;
        })
        .collect(Collectors.toList());
  }
  // @Transactional(readOnly = true)
  // public List<GuardDutyHistorydto> getGuardHistory(Long officerId) {
  // Officer officer = officerRepository.findById(officerId)
  // .orElseThrow(() -> new RuntimeException("Officer not found: " + officerId));
  // List<UserGuardAssignment> assignment = assignmentRepository.findAll();
  //
  // List<GuardDutyHistorydto> histories = assignment.stream()
  // .map(a -> {
  // GuardDutyHistorydto dutyHistory = new GuardDutyHistorydto();
  // dutyHistory.setAssignmentId(a.getId());
  // dutyHistory.setVipname(a.getCategory().getName());
  // dutyHistory.setDesignation(a.getCategory().getDesignation());
  // dutyHistory.setStartTime(a.getStartAt());
  // dutyHistory.setEndTime(a.getEndAt());
  // dutyHistory.setStatus(a.getStatus());
  // return dutyHistory;
  // })
  // .collect(Collectors.toList());
  //
  // GuardDutyHistorydto dutyHistory = new GuardDutyHistorydto();
  //
  // assignment.stream().map((a) -> {
  //
  // dutyHistory.setAssignmentId(assignment.getId());
  // dutyHistory.setVipname(assignment.getCategory().getName());
  // dutyHistory.setDesignation(assignment.getCategory().getDesignation());
  // dutyHistory.setStartTime(assignment.getStartAt());
  // dutyHistory.setEndTime(assignment.getEndAt());
  // dutyHistory.setStatus(assignment.getStatus());
  // });
  //

  // AssignmentResponsedto assignmentresponse = new AssignmentResponsedto();
  // assignmentresponse.setId(assignment.getId());
  // assignmentresponse.setEndAt(assignment.getEndAt());
  // assignmentresponse.setAtEnd(assignment.getAtEnd());
  // assignmentresponse.setStatus(assignment.getStatus());
  // assignmentresponse.setOfficer(assignment.getOfficer());
  // assignmentresponse.setCategory(assignment.getCategory());
  // assignmentresponse.setAssignedAt(assignment.getAssignedAt());
  // assignmentresponse.setStartAt(assignment.getStartAt());
  // return histories;
  // }
  //
  @Transactional(readOnly = true)
  public List<AssignmentHistoryEntity> getVipHistory(Long categoryId) {

    categoryRepository.findById(categoryId)
        .orElseThrow(() -> new RuntimeException(
            "Category not found for id: " + categoryId));

    List<AssignmentHistoryEntity> assignment = assignmentHistoryRepository.findAll().stream()
        .filter(a -> a.getCategory().stream()
            .anyMatch(c -> c.getId().equals(categoryId)))
        .toList();
    return assignment;
  }

  private CategoryDto mapCategory(Category c) {
    CategoryDto dto = new CategoryDto();
    dto.setId(c.getId());
    dto.setContactno(c.getContactno());
    dto.setEmail(c.getEmail());
    dto.setUsername(c.getUsername());
    dto.setDesignation(c.getDesignation());
    dto.setStatus(c.getStatus());
    dto.setAdharNo(c.getAdharNo());
    dto.setGender(c.getGender());
    return dto;
  }

  private OfficerDto mapOfficer(Officer o) {
    OfficerDto dto = new OfficerDto();
    dto.setId(o.getId());
    dto.setName(o.getName());
    dto.setRank(o.getRank());
    dto.setStatus(o.getStatus());
    dto.setEmail(o.getEmail());
    dto.setUsername(o.getUsername());
    dto.setCreatedTime(o.getCreatedTime());
    dto.setPnNumber(o.getPnNumber());
    dto.setAdharNo(o.getAdharNo());
    dto.setExperience(o.getExperience());
    dto.setContactno(o.getContactno());
    dto.setGender(o.getGender());
    return dto;
  }

  private AssignmentHistoryDto mapAssignmentHistory(UserGuardAssignment e) {

    AssignmentHistoryDto dto = new AssignmentHistoryDto();

    dto.setId(e.getId());
    dto.setStatus(e.getStatus());
    dto.setTimesAssigned(e.getTimesAssigned());
    dto.setAssignedAt(e.getAssignedAt());
    dto.setStartAt(e.getStartAt());
    dto.setEndAt(e.getEndAt());
    dto.setAtEnd(e.getAtEnd());

    // single category (VIP)
    if (e.getCategory() != null) {
      dto.setCategory(mapCategory(e.getCategory()));
    }

    // multiple officers
    if (e.getOfficers() != null && !e.getOfficers().isEmpty()) {
      dto.setOfficers(
          e.getOfficers().stream()
              .map(this::mapOfficer)
              .toList());
    }

    return dto;
  }

  private ReplacedOfficerDto mapReplacedOfficer(ReplacedOfficerEntity e) {

    ReplacedOfficerDto dto = new ReplacedOfficerDto();

    dto.setId(e.getId());
    dto.setReason(e.getReason());
    dto.setReasonMessage(e.getReasonMessage());
    dto.setAcceptedBy(e.getAcceptedBy());
    dto.setStatus(e.getStatus());

    if (e.getUserGuardAssignment() != null) {
      dto.setAssignmentId(e.getUserGuardAssignment().getId());
    }

    if (e.getPreviousOfficer() != null) {
      dto.setPreviousOfficer(mapOfficer(e.getPreviousOfficer()));
    }

    if (e.getCurrentOfficer() != null) {
      dto.setCurrentOfficer(mapOfficer(e.getCurrentOfficer()));
    }

    return dto;
  }

  public GuardHistoryResponse getAllHistory() {

    List<AssignmentHistoryDto> assignments = assignmentRepository.findAll()
        .stream()
        .map(this::mapAssignmentHistory)
        .toList();

    List<ReplacedOfficerDto> replaced = replacedOfficerRepository.findAll()
        .stream()
        .map(this::mapReplacedOfficer)
        .toList();

    return new GuardHistoryResponse(assignments, replaced);
  }

  //
  @Transactional
  public AssignmentResponsedto completeDutyForCategory(Long categoryId, String status) {
    // 1. Load category
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new RuntimeException(
            "Category not found for id: " + categoryId));

    // 2. Get all ACTIVE-DUTY assignments under this category
    UserGuardAssignment activeAssignments = assignmentRepository
        .findByCategoryIdAndStatusIgnoreCase(categoryId,
            ACTIVE_STATUS);

    if (activeAssignments == null) {
      // nothing active → mark VIP inactive and return empty
      if (false) {
        NotificationManagement existingNotificationUser = notificationManagementRepo
            .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(1L,
                "user");
        String userFcmToken = existingNotificationUser != null
            ? existingNotificationUser.getNotificationToken()
            : null;

        Long userId = existingNotificationUser != null
            ? existingNotificationUser.getNotificationSenderId()
            : null;

        service.sendNotificationSafely(
            userFcmToken,
            "Dugy Decided",
            "Please Check The Duty Decision ",
            "Manager",
            userId);

        NotificationManagement existingNotificationAdmin = notificationManagementRepo
            .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(1L,
                "admin");
        String adminFcmToken = existingNotificationAdmin != null
            ? existingNotificationAdmin.getNotificationToken()
            : null;
        Long adminId = existingNotificationAdmin != null
            ? existingNotificationAdmin.getNotificationSenderId()
            : null;

        service.sendNotificationSafely(
            adminFcmToken,
            "Dugy Decided",
            "Please Check The Duty Decision ",
            "admin",
            adminId);
        // ✅ Send notification immediately
      }

      category.setStatus("Inactive");
      categoryRepository.save(category);

      AssignmentResponse resp = new AssignmentResponse();
      resp.setSummary(Collections.emptyList());
      resp.setDetails(Collections.emptyList());
      return null;
    }

    // 3. Mark every assignment as completed/inactive
    LocalDateTime now = LocalDateTime.now();
    Map<Long, Officer> officersToFree = new HashMap<>();

    activeAssignments.setStatus(status);
    activeAssignments.setAtEnd(now);

    // 4. Free all these officers and send notifications
    for (Officer officer : activeAssignments.getOfficers()) {
      if (officer == null) {
        continue;
      }
      officer.setStatus("inactive");

      NotificationManagement notificationofficer = new NotificationManagement();
      NotificationManagement notificationidoff = notificationManagementRepo
          .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(officer.getId(),
              "guard");

      notificationofficer.setNotificationSenderId(officer.getId());
      notificationofficer.setNotificationSender("guard");
      notificationofficer.setNotificationSenderName(officer.getName());
      notificationofficer.setNotificationMessage("Your duty Status is ----" +
          status);

      if (notificationidoff != null) {
        notificationofficer.setNotificationToken(notificationidoff.getNotificationToken());
      }

      notificationofficer.setNotificationStatus(false);
      notificationofficer.setNotificationAssignTime(LocalDateTime.now());
      service.sendNotificationSafely(
          notificationidoff != null ? notificationidoff.getNotificationToken() : null,
          status,
          "Check Your Duty Completion Status Over the Portal",
          "officer",
          officer.getId());

      notificationManagementRepo.save(notificationofficer);
      officerRepository.save(officer);
    }
    if (true) {

      UserEntity user = userRepository.findById(1L).orElse(null);
      NotificationManagement existingNotificationUser = notificationManagementRepo
          .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(1L,
              "user");
      if (user != null) {
        NotificationManagement notificationuser = new NotificationManagement();
        notificationuser.setNotificationSenderId(user.getId());
        notificationuser.setNotificationSender("user");
        notificationuser.setNotificationSenderName(user.getName());
        notificationuser
            .setNotificationMessage("duty status of the vip is changed where vip name is ==> " + category.getName());

        notificationuser.setNotificationStatus(false);
        notificationuser.setNotificationAssignTime(LocalDateTime.now());
        notificationManagementRepo.save(notificationuser);
      }
      if (existingNotificationUser != null) {
        existingNotificationUser.setNotificationToken(existingNotificationUser.getNotificationToken());
      }

      AdminEntity admin = adminRepository.findById(1L).orElse(null);
      NotificationManagement existingNotificationAdmin = notificationManagementRepo
          .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(1L,
              "admin");
      NotificationManagement notificationadmin = new NotificationManagement();
      notificationadmin.setNotificationSenderId(admin.getId());
      notificationadmin.setNotificationSender("admin");
      notificationadmin.setNotificationSenderName(admin.getAdminName());
      notificationadmin
          .setNotificationMessage("duty status of the vip is changed where vip name is ==> " + category.getName());

      if (existingNotificationAdmin != null) {
        existingNotificationAdmin.setNotificationToken(existingNotificationAdmin.getNotificationToken());
      }

      notificationadmin.setNotificationStatus(false);
      notificationadmin.setNotificationAssignTime(LocalDateTime.now());
      notificationManagementRepo.save(notificationadmin);
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
          userFcmToken,
          "Duty completion status updated",
          "Please Check The Stauts by the portal ",
          "Manager",
          userId);

      service.sendNotificationSafely(
          adminFcmToken,
          "Duty completion status updated",
          "Please Check The Stauts by the portal ",
          "admin",
          adminId);

      // Use helper method to send notification
    }

    // Long assignId = category.getId() != null ?
    // category.getUserGuardAssignment().getId() : null;
    // UserGuardAssignment uga = category.getUserGuardAssignment();

    // 5. Mark VIP/category as Inactive
    category.setStatus("Inactive");
    // if (uga != null) {
    // category.setUserGuardAssignment(null);
    // }
    categoryRepository.save(category);

    NotificationManagement notificationofficer = new NotificationManagement();
    NotificationManagement notificationid = notificationManagementRepo
        .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(category.getId(),
            "vip");

    notificationofficer.setNotificationSenderId(category.getId());
    notificationofficer.setNotificationSender("vip");
    notificationofficer.setNotificationSenderName(category.getName());
    notificationofficer.setNotificationMessage("Your duty Status is ----" +
        status);

    if (notificationid != null) {
      notificationofficer.setNotificationToken(notificationid.getNotificationToken());
    }

    notificationofficer.setNotificationStatus(false);
    notificationofficer.setNotificationAssignTime(LocalDateTime.now());
    notificationManagementRepo.save(notificationofficer);

    // Use helper method to send notification
    service.sendNotificationSafely(
        notificationid != null ? notificationid.getNotificationToken() : null,
        status,
        "Check Your Duty Completion Status Over the Portal",
        "category",
        category.getId());

    // 6. Return updated snapshot

    // AssignmentHistoryEntity historyEntity = new AssignmentHistoryEntity();
    //
    // historyEntity.setCategory(
    // activeAssignments.getCategory() != null
    // ? new ArrayList<>(activeAssignments.getCategory())
    // : new ArrayList<>());
    //
    // historyEntity.setOfficer(
    // activeAssignments.getOfficer() != null
    // ? new ArrayList<>(activeAssignments.getOfficer())
    // : new ArrayList<>());
    // historyEntity.setAssignedAt(activeAssignments.getAssignedAt());
    // historyEntity.setAtEnd(activeAssignments.getAtEnd());
    // historyEntity.setStatus(activeAssignments.getStatus());
    // historyEntity.setStartAt(activeAssignments.getStartAt());
    // historyEntity.setEndAt(LocalDateTime.now());
    //
    // assignmentHistoryRepository.save(historyEntity);
    AssignmentResponsedto response = new AssignmentResponsedto();
    response.setStartAt(activeAssignments.getStartAt());
    response.setAssignedAt(activeAssignments.getAssignedAt());
    response.setAtEnd(activeAssignments.getAtEnd());
    response.setId(activeAssignments.getId());
    assignmentRepository.save(activeAssignments);
    return response;
  }

  @Transactional
  public ReplacedOfficerEntity markGuardOnLeaveAndRefillByOfficer(Long officerId,
      String reason, String accepter,
      String Message, int val) {

    Officer officer = officerRepository.findById(officerId)
        .orElseThrow(() -> new RuntimeException("Officer not found with id: " + officerId));

    UserGuardAssignment assignment = officer.getAssignments().stream()
        .filter(a -> ACTIVE_STATUS.equalsIgnoreCase(a.getStatus())).findFirst().get();
    // UserGuardAssignment assignment = assignmentRepository
    // .findFirstByOfficerIdAndStatusOrderByAssignedAtDesc(
    // officerId,
    // ACTIVE_STATUS)
    // .orElseThrow(() -> new RuntimeException(
    // "No active assignment found for guard id: "
    // + officerId));
    // System.out.println("reason in the mark function " + reason);
    //
    ReplacedOfficerEntity replacedOfficer = new ReplacedOfficerEntity();
    replacedOfficer.setPreviousOfficer(officer);
    replacedOfficer.setUserGuardAssignment(assignment);
    replacedOfficer.setReason(reason);
    replacedOfficer.setReasonMessage(Message);
    replacedOfficer.setAcceptedBy(accepter);
    replacedOfficer.setStatus("Pending");

    // assignment.setStatus(reason);

    // System.out.println("officer is " + officer +
    // "officer status is " + officer.getStatus() + "Assignment is this " +
    // assignment);
    officer.setStatus("Inactive");
    // officer.setUserGuardAssignment(null);
    System.out.println(officer.getStatus() + "Status is this" +
        officer.getStatus());
    Officer officer2 = officerRepository.save(officer);
    System.out.println(officer2 + "officer 2 is this " + "officer 2 is this " +
        officer2.getStatus());

    // int willAssign = Math.min(missing, combinedPool.size());
    if (val == 0) {

      replacedOfficer.setCurrentOfficer(null);
      replacedOfficer.setStatus("Accepted");
      replacedOfficerRepository.save(replacedOfficer);

    } else {
      List<Officer> allInactiveThisLevel = officerRepository.findByRank(
          officer.getRank())
          .stream()
          .filter(o -> "Inactive".equalsIgnoreCase(
              o.getStatus()))
          .collect(Collectors.toList());
      int activeCount = allInactiveThisLevel.size();

      List<Officer> combinedPool = new ArrayList<>(allInactiveThisLevel);
      Collections.shuffle(combinedPool);

      List<Officer> selected = combinedPool.subList(0, val);
      Officer selectedOfficer = selected.get(0);

      if (assignment.getOfficers() == null) {
        assignment.setOfficers(new ArrayList<>());
      }
      assignment.getOfficers().add(selectedOfficer);

      selectedOfficer.setStatus("Active");
      selectedOfficer.getAssignments().add(assignment);
      officerRepository.save(selectedOfficer);

      replacedOfficer.setCurrentOfficer(selectedOfficer);
      replacedOfficer.setStatus("Accepted");
      assignmentRepository.save(assignment);
      replacedOfficerRepository.save(replacedOfficer);

      // Long vipId = assignment.getCategory().stream().findFirst().get().getId();
      String level = officer.getRank(); // or
      // assignment.getGuardLevel()
      // if you have that
      // field
    }
    // 1 guard left -> missing = 1
    return replacedOfficer;
  }
  //
  // /**
  // * Safely send FCM notification with null/empty token validation
  // */

}
