package com.infotech.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.infotech.dto.AssignmentResponse;
import com.infotech.dto.AssignmentSummary;
import com.infotech.dto.GuardAssignmentRequest;
import com.infotech.dto.GuardLevelRequest;
import com.infotech.dto.OfficerDuty;
import com.infotech.entity.Category;
import com.infotech.entity.NotificationManagement;
import com.infotech.entity.Officer;
import com.infotech.entity.UserGuardAssignment;
import com.infotech.repository.CategoryRepository;
import com.infotech.repository.NotificationCategoryRepository;
import com.infotech.repository.NotificationGuardRepository;
import com.infotech.repository.NotificationManagementRepository;
import com.infotech.repository.OfficerRepository;
import com.infotech.repository.UserGuardAssignmentRepository;

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

  private final FcmService service;

  @PersistenceContext
  private EntityManager em;

  private static final int MAX_REUSE = 10;

  // -----------------------------------------------
  // assignGuardsAutomatically (unchanged)
  // -----------------------------------------------
  @Transactional
  public AssignmentResponse assignGuardsAutomatically(GuardAssignmentRequest request) {

    Category category = categoryRepository.findById(request.getUserId())
        .orElseThrow(() -> new RuntimeException(
            "Category (user) not found"));

    List<UserGuardAssignment> responseDetails = new ArrayList<>();
    List<AssignmentSummary> summaries = new ArrayList<>();

    Map<String, List<Officer>> selectionsByLevel = new LinkedHashMap<>();
    Map<String, Integer> requestedByLevel = new HashMap<>();

    for (GuardLevelRequest levelReq : request.getLevels()) {
      String level = levelReq.getGuardLevel();
      int requestedCount = levelReq.getNumberOfGuards();
      requestedByLevel.put(level, requestedCount);

      List<UserGuardAssignment> activeForLevel = assignmentRepository
          .findByCategoryAndGuardLevelAndStatus(
              category,
              level,
              "Active");

      int activeCount = activeForLevel.size();

      if (activeCount >= requestedCount) {
        summaries.add(new AssignmentSummary(level,
            requestedCount,
            activeCount,
            0));
        selectionsByLevel.put(level, Collections.emptyList());
        continue;
      }

      int missing = requestedCount - activeCount;

      List<UserGuardAssignment> previous = assignmentRepository
          .findByCategoryAndGuardLevel(category,
              level);

      Set<Long> activeGuardIds = previous.stream()
          .filter(a -> "Active".equalsIgnoreCase(a
              .getStatus()))
          .map(a -> a.getOfficer().getId())
          .collect(Collectors.toSet());

      List<UserGuardAssignment> inactivePrevious = previous.stream()
          .filter(a -> !"Active".equalsIgnoreCase(
              a.getStatus()))
          .collect(Collectors.toList());

      List<Officer> allInactiveThisLevel = officerRepository.findByRank(
          level)
          .stream()
          .filter(o -> "Inactive".equalsIgnoreCase(
              o.getStatus()))
          .collect(Collectors.toList());

      List<Officer> availableGuards = allInactiveThisLevel.stream()
          .filter(o -> !activeGuardIds.contains(o
              .getId()))
          .collect(Collectors.toList());

      Map<Long, Integer> timesMap = inactivePrevious.stream()
          .collect(Collectors.toMap(
              a -> a.getOfficer().getId(),
              UserGuardAssignment::getTimesAssigned,
              Math::max));

      Set<Long> excluded = timesMap.entrySet()
          .stream()
          .filter(e -> e.getValue() >= MAX_REUSE)
          .map(Map.Entry::getKey)
          .collect(Collectors.toSet());

      List<Officer> cleaned = availableGuards.stream()
          .filter(o -> !excluded.contains(o
              .getId()))
          .collect(Collectors.toList());

      List<Officer> reusable = inactivePrevious.stream()
          .filter(a -> a.getTimesAssigned() < MAX_REUSE)
          .map(UserGuardAssignment::getOfficer)
          .filter(o -> !excluded.contains(o
              .getId()))
          .collect(Collectors.toList());

      Map<Long, Officer> combined = new LinkedHashMap<>();
      cleaned.forEach(o -> combined.put(o.getId(), o));
      reusable.forEach(o -> combined.putIfAbsent(o.getId(), o));

      List<Officer> combinedPool = new ArrayList<>(combined.values());
      Collections.shuffle(combinedPool);

      int willAssign = Math.min(missing, combinedPool.size());
      List<Officer> selected = combinedPool.subList(0, willAssign);

      selectionsByLevel.put(level, selected);

      int totalAssignedNow = activeCount + willAssign;
      summaries.add(new AssignmentSummary(level, requestedCount,
          totalAssignedNow,
          Math.max(0, requestedCount - totalAssignedNow)));
    }

    List<String> failLevels = summaries.stream()
        .filter(s -> s.getMissing() > 0)
        .map(AssignmentSummary::getLevel)
        .collect(Collectors.toList());

    if (!failLevels.isEmpty()) {
      throw new RuntimeException("Insufficient guards for: " + failLevels
          + ". No assignment performed.");
    }

    for (Map.Entry<String, List<Officer>> entry : selectionsByLevel.entrySet()) {
      String level = entry.getKey();
      List<Officer> selected = entry.getValue();

      if (selected.isEmpty()) {
        List<UserGuardAssignment> activeOld = assignmentRepository
            .findByCategoryAndGuardLevelAndStatus(
                category,
                level,
                "Active");
        responseDetails.addAll(activeOld);
        continue;
      }

      List<UserGuardAssignment> previous = assignmentRepository
          .findByCategoryAndGuardLevel(category,
              level);

      List<UserGuardAssignment> inactivePrev = previous.stream()
          .filter(a -> !"Active".equals(a.getStatus()))
          .collect(Collectors.toList());

      List<UserGuardAssignment> created = new ArrayList<>();

      for (Officer officer : selected) {
        officer.setStatus("Active");
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
        sendNotificationSafely(
            officerFcmToken,
            "Duty Assign",
            "Go And Check Your Duty From The Portal",
            "officer",
            officer.getId());

        Optional<UserGuardAssignment> exist = inactivePrev
            .stream()
            .filter(a -> a.getOfficer().getId()
                .equals(officer.getId()))
            .findFirst();

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

        UserGuardAssignment a;
        if (exist.isPresent()) {
          a = exist.get();
          a.setTimesAssigned(a.getTimesAssigned() + 1);
        } else {
          a = new UserGuardAssignment();
          a.setCategory(category);
          a.setOfficer(officer);
          a.setTimesAssigned(1);
        }

        a.setStatus("Active Duty");
        a.setAssignedAt(LocalDateTime.now());
        a.setStartAt(request.getStartAt());
        a.setEndAt(request.getEndAt());

        created.add(a);
      }
      if (!created.isEmpty()) {
        responseDetails.addAll(assignmentRepository
            .saveAll(created));
      }

      responseDetails.addAll(assignmentRepository
          .findByCategoryAndGuardLevelAndStatus(
              category,
              level,
              "Active"));
    }

    if (!responseDetails.isEmpty()) {
      category.setStatus("Active");
      categoryRepository.save(category);
    }

    // BEFORE creating new notification, look up existing token
    NotificationManagement existingVipNotification = notificationManagementRepo
        .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(category.getId(), "vip");

    String vipFcmToken = existingVipNotification != null
        ? existingVipNotification.getNotificationToken()
        : null;

    // Send notification with existing token
    sendNotificationSafely(
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
    AssignmentResponse resp = new AssignmentResponse();
    resp.setSummary(summaries);
    resp.setDetails(responseDetails);

    return resp;
  } // -----------------------------------------------
  // getAssignmentResponseForCategory (unchanged)
  // -----------------------------------------------
  // inside AssignmentService

  private static final String ACTIVE_STATUS = "Active Duty";

  @Transactional(readOnly = true)
  public AssignmentResponse getAssignmentResponseForCategory(Long categoryId) {

    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new RuntimeException(
            "Category not found for id: " + categoryId));

    // Load all assignments for this category, officer is
    // fetched via @EntityGraph
    List<UserGuardAssignment> all = assignmentRepository.findByCategoryId(
        categoryId);

    // Keep only active-duty rows
    List<UserGuardAssignment> active = Optional.ofNullable(all)
        .orElse(Collections.emptyList())
        .stream()
        .filter(a -> ACTIVE_STATUS.equalsIgnoreCase(
            a.getStatus()))
        .collect(Collectors.toList());

    // Group by rank for summary
    Map<String, Long> byLevel = active.stream()
        .collect(Collectors.groupingBy(
            a -> a.getOfficer() != null
                ? a.getOfficer().getRank()
                : "UNASSIGNED",
            Collectors.counting()));

    List<AssignmentSummary> summaries = byLevel.entrySet().stream()
        .map(e -> new AssignmentSummary(
            e.getKey(),
            e.getValue().intValue(), // requested
                                     // (or
                                     // 0
                                     // if
                                     // you
                                     // prefer)
            e.getValue().intValue(), // assigned
            0 // missing
        ))
        .collect(Collectors.toList());

    AssignmentResponse resp = new AssignmentResponse();
    resp.setSummary(summaries);

    // 🔧 FIX: use the SAME status here as above
    resp.setDetails(active);

    return resp;
  }

  // -----------------------------------------------
  // getVipForGuard (unchanged)
  // -----------------------------------------------
  @Transactional(readOnly = true)
  public OfficerDuty getVipForGuard(Long officerId) {
    OfficerDuty dto = new OfficerDuty();

    UserGuardAssignment assignment = assignmentRepository
        .findFirstByOfficerIdAndStatusOrderByAssignedAtDesc(
            officerId,
            "Active Duty")
        .orElseThrow(() -> new RuntimeException(
            "No active VIP assignment for guard "
                + officerId));

    Category cat = assignment.getCategory();

    dto.setName(cat.getName());
    dto.setDesignation(cat.getDesignation());
    dto.setStartAt(assignment.getStartAt());
    dto.setEndAt(assignment.getEndAt());
    dto.setId(assignment.getId());

    return dto;
  }

  // ============================================================
  // ✔ FIXED: markGuardOnLeaveAndRefill
  // ============================================================
  @Transactional
  public AssignmentResponse markGuardOnLeaveAndRefillit(Long assignmentId,
      GuardAssignmentRequest requirement) {

    UserGuardAssignment assignment = assignmentRepository.findById(assignmentId)
        .orElseThrow(() -> new RuntimeException(
            "Assignment not found"));

    // Only THIS assignment becomes Inactive
    assignment.setStatus("Inactive");
    assignmentRepository.save(assignment);

    // Guard itself becomes inactive
    Officer officer = assignment.getOfficer();
    officer.setStatus("Inactive");
    officerRepository.save(officer);

    Long vipId = assignment.getCategory().getId();

    NotificationManagement notificationManagement = new NotificationManagement();
    NotificationManagement notificationmange = notificationManagementRepo
        .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(vipId, "vip");

    notificationManagement.setNotificationSender("vip");
    notificationManagement.setNotificationSenderId(vipId);
    notificationManagement.setNotificationMessage("Duty Completed");
    notificationManagement.setNotificationStatus(false);
    notificationManagement.setNotificationSenderName(assignment.getCategory()
        .getName());

    notificationManagement.setNotificationAssignTime(LocalDateTime.now());

    if (notificationmange != null) {
      notificationManagement.setNotificationToken(notificationmange.getNotificationToken());
    }

    // service.send(notificationman.getNotificationToken(), "Duty Assign", "For More
    // Detail Check It From The Portal");
    if (notificationmange != null) {
      try {
        service.send(
            notificationmange.getNotificationToken(),
            "Duty Assign",
            "For More Detail Check It From The Portal");
      } catch (Exception e) {
        log.error("FCM send failed for vip {}", vipId, e);
      }
    }

    notificationManagementRepo.save(notificationManagement);

    return refillMissingGuards(vipId, requirement);
  }

  @Transactional
  public AssignmentResponse markGuardOnLeaveAndRefill(Long vipId, String level,
      int missing) {

    Category category = categoryRepository.findById(vipId)
        .orElseThrow(() -> new RuntimeException(
            "Category not found"));

    List<UserGuardAssignment> responseDetails = new ArrayList<>();
    List<AssignmentSummary> summaries = new ArrayList<>();

    // Get currently active
    List<UserGuardAssignment> active = assignmentRepository
        .findByCategoryAndGuardLevelAndStatus(category, level,
            "Active");

    int activeCount = active.size();

    // If nothing is actually missing, just return summary +
    // current actives
    if (missing <= 0) {
      summaries.add(new AssignmentSummary(
          level,
          activeCount + missing, // requested
                                 // total
          activeCount, // actually
                       // active
          0 // shortfall
      ));
      responseDetails.addAll(active);
      return new AssignmentResponse(summaries, responseDetails);
    }

    // Previous assignments (any status)
    List<UserGuardAssignment> previous = assignmentRepository
        .findByCategoryAndGuardLevel(category, level);

    Set<Long> activeIds = previous.stream()
        .filter(a -> "Active".equals(a.getStatus()))
        .map(a -> a.getOfficer().getId())
        .collect(Collectors.toSet());

    // Inactive officers of this rank
    List<Officer> inactiveOfficers = officerRepository.findByRank(level)
        .stream()
        .filter(o -> "Inactive".equals(o.getStatus()))
        .collect(Collectors.toList());

    // Available officers (not already active)
    List<Officer> available = inactiveOfficers.stream()
        .filter(o -> !activeIds.contains(o.getId()))
        .collect(Collectors.toList());

    // RANDOM selection
    Collections.shuffle(available);
    List<Officer> selected = available.subList(0, Math.min(missing, available
        .size()));

    List<UserGuardAssignment> created = new ArrayList<>();
    for (Officer officer : selected) {

      officer.setStatus("Active");
      officerRepository.save(officer);

      UserGuardAssignment newAssign = new UserGuardAssignment();
      newAssign.setCategory(category);
      newAssign.setOfficer(officer);
      newAssign.setStatus("Active Duty");
      newAssign.setAssignedAt(LocalDateTime.now());

      // Reuse timing from old
      // active assignment
      if (!active.isEmpty()) {
        newAssign.setStartAt(active.get(0).getStartAt());
        newAssign.setEndAt(active.get(0).getEndAt());
      }

      created.add(newAssign);
    }

    if (!created.isEmpty()) {
      responseDetails.addAll(assignmentRepository.saveAll(created));
    }

    // Get final list of active
    List<UserGuardAssignment> nowActive = assignmentRepository
        .findByCategoryAndGuardLevelAndStatus(category, level,
            "Active");

    summaries.add(new AssignmentSummary(
        level,
        activeCount + missing, // required
                               // total
        nowActive.size(), // actually
                          // active
                          // now
        Math.max(0, (activeCount + missing) - nowActive.size()) // still
                                                                // short
    ));

    responseDetails.addAll(nowActive);

    AssignmentResponse resp = new AssignmentResponse();
    resp.setSummary(summaries);
    resp.setDetails(responseDetails);

    return resp;
  }

  // ============================================================
  // ✔ FIXED: refillMissingGuards (raw entity list, no DTOs)
  // ============================================================
  @Transactional
  public AssignmentResponse refillMissingGuards(Long vipId, GuardAssignmentRequest requirement) {

    Category category = categoryRepository.findById(vipId)
        .orElseThrow(() -> new RuntimeException(
            "Category not found"));

    List<UserGuardAssignment> responseDetails = new ArrayList<>();
    List<AssignmentSummary> summaries = new ArrayList<>();

    for (GuardLevelRequest levelReq : requirement.getLevels()) {

      String level = levelReq.getGuardLevel();
      int requestedCount = levelReq.getNumberOfGuards();

      List<UserGuardAssignment> active = assignmentRepository
          .findByCategoryAndGuardLevelAndStatus(
              category,
              level,
              "Active");

      int activeCount = active.size();
      int missing = requestedCount - activeCount;

      if (missing <= 0) {
        summaries.add(new AssignmentSummary(level,
            requestedCount,
            activeCount,
            0));
        responseDetails.addAll(active);
        continue;
      }

      List<UserGuardAssignment> previous = assignmentRepository
          .findByCategoryAndGuardLevel(category,
              level);

      Set<Long> activeGuardIds = previous.stream()
          .filter(a -> "Active".equals(a.getStatus()))
          .map(a -> a.getOfficer().getId())
          .collect(Collectors.toSet());

      List<UserGuardAssignment> inactivePrev = previous.stream()
          .filter(a -> !"Active".equals(a.getStatus()))
          .collect(Collectors.toList());

      List<Officer> inactiveOfficers = officerRepository.findByRank(level)
          .stream()
          .filter(o -> "Inactive".equals(o.getStatus()))
          .collect(Collectors.toList());

      List<Officer> available = inactiveOfficers.stream()
          .filter(o -> !activeGuardIds.contains(o
              .getId()))
          .collect(Collectors.toList());

      Map<Long, Integer> timesMap = inactivePrev.stream()
          .collect(Collectors.toMap(
              a -> a.getOfficer().getId(),
              UserGuardAssignment::getTimesAssigned,
              Math::max));

      Set<Long> excluded = timesMap.entrySet().stream()
          .filter(e -> e.getValue() >= MAX_REUSE)
          .map(Map.Entry::getKey)
          .collect(Collectors.toSet());

      List<Officer> cleaned = available.stream()
          .filter(o -> !excluded.contains(o
              .getId()))
          .collect(Collectors.toList());

      List<Officer> reusable = inactivePrev.stream()
          .filter(a -> a.getTimesAssigned() < MAX_REUSE)
          .map(UserGuardAssignment::getOfficer)
          .filter(o -> !excluded.contains(o
              .getId()))
          .collect(Collectors.toList());

      Map<Long, Officer> combined = new LinkedHashMap<>();
      cleaned.forEach(o -> combined.put(o.getId(), o));
      reusable.forEach(o -> combined.putIfAbsent(o.getId(), o));

      List<Officer> pool = new ArrayList<>(combined.values());
      Collections.shuffle(pool);

      int willAssign = Math.min(missing, pool.size());
      List<Officer> selected = pool.subList(0, willAssign);

      List<UserGuardAssignment> created = new ArrayList<>();

      for (Officer officer : selected) {
        officer.setStatus("Active");
        officerRepository.save(officer);

        Optional<UserGuardAssignment> exist = inactivePrev
            .stream()
            .filter(a -> a.getOfficer().getId()
                .equals(officer.getId()))
            .findFirst();

        UserGuardAssignment a;
        if (exist.isPresent()) {
          a = exist.get();
          a.setTimesAssigned(a.getTimesAssigned()
              + 1);
        } else {
          a = new UserGuardAssignment();
          a.setCategory(category);
          a.setOfficer(officer);
          a.setTimesAssigned(1);
        }

        a.setStatus("Active");
        a.setAssignedAt(LocalDateTime.now());

        if (!active.isEmpty()) {
          a.setStartAt(active.get(0).getStartAt());
          a.setEndAt(active.get(0).getEndAt());
        }

        created.add(a);
      }

      if (!created.isEmpty()) {
        responseDetails.addAll(assignmentRepository
            .saveAll(created));
      }

      List<UserGuardAssignment> nowActive = assignmentRepository
          .findByCategoryAndGuardLevelAndStatus(
              category,
              level,
              "Active");

      summaries.add(new AssignmentSummary(level,
          requestedCount,
          nowActive.size(),
          Math.max(0, requestedCount - nowActive
              .size())));

      responseDetails.addAll(nowActive);
    }

    category.setStatus("Active");
    categoryRepository.save(category);

    AssignmentResponse resp = new AssignmentResponse();
    resp.setSummary(summaries);
    resp.setDetails(responseDetails);

    return resp;
  }

  @Transactional(readOnly = true)
  public List<UserGuardAssignment> getHistory(Officer officer) {

    List<UserGuardAssignment> assignment = assignmentRepository
        .findByOfficer(officer);
    return assignment;
  }

  @Transactional(readOnly = true)
  public List<UserGuardAssignment> getGuardHistory(Long officerId) {
    return assignmentRepository.findByOfficerId(officerId);
  }

  @Transactional(readOnly = true)
  public List<UserGuardAssignment> getVipHistory(Long categoryId) {
    return assignmentRepository.findByCategoryId(categoryId);
  }

  public List<UserGuardAssignment> getAllHistory() {
    return assignmentRepository.findAll();
  }

  @Transactional
  public AssignmentResponse completeDutyForCategory(Long categoryId, String status) {
    // 1. Load category
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new RuntimeException(
            "Category not found for id: " + categoryId));

    // 2. Get all ACTIVE-DUTY assignments under this category
    List<UserGuardAssignment> activeAssignments = assignmentRepository
        .findByCategoryIdAndStatusIgnoreCase(categoryId,
            ACTIVE_STATUS);

    if (activeAssignments.isEmpty()) {
      // nothing active → mark VIP inactive and return empty
      category.setStatus("Inactive");
      categoryRepository.save(category);

      AssignmentResponse resp = new AssignmentResponse();
      resp.setSummary(Collections.emptyList());
      resp.setDetails(Collections.emptyList());
      return resp;
    }

    // 3. Mark every assignment as completed/inactive
    LocalDateTime now = LocalDateTime.now();
    Map<Long, Officer> officersToFree = new HashMap<>();

    for (UserGuardAssignment uga : activeAssignments) {
      uga.setStatus(status);
      uga.setAtEnd(now);
      assignmentRepository.save(uga);

      Officer officer = uga.getOfficer();
      if (officer != null) {
        officersToFree.put(officer.getId(), officer);
      }
    }

    // 4. Free all these officers and send notifications
    for (Officer officer : officersToFree.values()) {
      officer.setStatus("Inactive");

      NotificationManagement notificationofficer = new NotificationManagement();
      NotificationManagement notificationidoff = notificationManagementRepo
          .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(officer.getId(),
              "guard");

      notificationofficer.setNotificationSenderId(officer.getId());
      notificationofficer.setNotificationSender("guard");
      notificationofficer.setNotificationSenderName(officer.getName());
      notificationofficer.setNotificationMessage("Your duty Status is ----" + status);

      if (notificationidoff != null) {
        notificationofficer.setNotificationToken(notificationidoff.getNotificationToken());
      }

      notificationofficer.setNotificationStatus(false);
      notificationofficer.setNotificationAssignTime(LocalDateTime.now());
      notificationManagementRepo.save(notificationofficer);

      // Use helper method to send notification
      sendNotificationSafely(
          notificationidoff != null ? notificationidoff.getNotificationToken() : null,
          status,
          "Check Your Duty Completion Status Over the Portal",
          "officer",
          officer.getId());

      officerRepository.save(officer);
    }

    // 5. Mark VIP/category as Inactive
    category.setStatus("Inactive");
    categoryRepository.save(category);

    NotificationManagement notificationofficer = new NotificationManagement();
    NotificationManagement notificationid = notificationManagementRepo
        .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(category.getId(),
            "vip");

    notificationofficer.setNotificationSenderId(category.getId());
    notificationofficer.setNotificationSender("vip");
    notificationofficer.setNotificationSenderName(category.getName());
    notificationofficer.setNotificationMessage("Your duty Status is ----" + status);

    if (notificationid != null) {
      notificationofficer.setNotificationToken(notificationid.getNotificationToken());
    }

    notificationofficer.setNotificationStatus(false);
    notificationofficer.setNotificationAssignTime(LocalDateTime.now());
    notificationManagementRepo.save(notificationofficer);

    // Use helper method to send notification
    sendNotificationSafely(
        notificationid != null ? notificationid.getNotificationToken() : null,
        status,
        "Check Your Duty Completion Status Over the Portal",
        "category",
        category.getId());

    // 6. Return updated snapshot
    return getAssignmentResponseForCategory(categoryId);
  }

  @Transactional
  public AssignmentResponse markGuardOnLeaveAndRefillByOfficer(Long officerId) {
    UserGuardAssignment assignment = assignmentRepository
        .findFirstByOfficerIdAndStatusOrderByAssignedAtDesc(
            officerId,
            ACTIVE_STATUS)
        .orElseThrow(() -> new RuntimeException(
            "No active assignment found for guard id: "
                + officerId));

    assignment.setStatus("Incident Occur");
    assignmentRepository.save(assignment);

    Officer officer = assignment.getOfficer();
    officer.setStatus("Inactive");
    officerRepository.save(officer);

    Long vipId = assignment.getCategory().getId();
    String level = officer.getRank(); // or
                                      // assignment.getGuardLevel()
                                      // if you have that
                                      // field

    // 1 guard left -> missing = 1
    return markGuardOnLeaveAndRefill(vipId, level, 1);
  }

  /**
   * Safely send FCM notification with null/empty token validation
   */
  private void sendNotificationSafely(String token, String title, String body, String entityType, Long entityId) {
    if (token == null || token.trim().isEmpty()) {
      log.warn("Skipping FCM notification for {} {}: Token is null or empty", entityType, entityId);
      return;
    }

    try {
      service.send(token, title, body);
      log.info("Successfully sent FCM notification to {} {}: {}   token is =={}==", entityType, entityId, title, token);
    } catch (IllegalArgumentException e) {
      // Specifically catch the token validation error
      log.error("Invalid FCM token for {} {}: {}", entityType, entityId, e.getMessage());
    } catch (Exception e) {
      log.error("FCM send failed for {} {}: {}", entityType, entityId, e.getMessage(), e);
    }
  }

}
