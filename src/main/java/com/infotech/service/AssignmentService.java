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
import jakarta.persistence.TypedQuery;

import com.infotech.dto.AssignmentResponse;
import com.infotech.dto.AssignmentSummary;
import com.infotech.dto.GuardAssignmentRequest;
import com.infotech.dto.GuardLevelRequest;
import com.infotech.entity.Category;
import com.infotech.entity.Officer;
import com.infotech.entity.UserGuardAssignment;
import com.infotech.repository.CategoryRepository;
import com.infotech.repository.OfficerRepository;
import com.infotech.repository.UserGuardAssignmentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final CategoryRepository categoryRepository;
    private final OfficerRepository officerRepository;
    private final UserGuardAssignmentRepository assignmentRepository;

    @PersistenceContext
    private EntityManager em;

    private static final int MAX_REUSE = 4;

    /**
     * Idempotent assignment: if active assignments already satisfy the requested
     * number for a level,
     * no new assignments are created for that level. Only missing slots are filled.
     */
    // @Transactional
    // public AssignmentResponse assignGuardsAutomatically(GuardAssignmentRequest
    // request) {
    //
    // System.out.println(request.getUserId());
    // System.out.println(request.getLevels());
    // System.out.println(request.getAtEnd());
    // System.out.println(request.getStartAt());
    // System.out.println(request.getEndAt());
    //
    // Category category = categoryRepository.findById(request.getUserId())
    // .orElseThrow(() -> new RuntimeException("Category (user) not found"));
    //
    // List<UserGuardAssignment> responseDetails = new ArrayList<>();
    // List<AssignmentSummary> summaries = new ArrayList<>();
    //
    // for (GuardLevelRequest levelReq : request.getLevels()) {
    //
    // String level = levelReq.getGuardLevel();
    // int requestedCount = levelReq.getNumberOfGuards();
    //
    // // 1) Current active assignments for this category+level
    // List<UserGuardAssignment> activeForLevel = assignmentRepository
    // .findByCategoryAndGuardLevelAndStatus(category, level, "Active");
    //
    // int activeCount = activeForLevel.size();
    //
    // // If already satisfied — add summary & include existing assignments
    // if (activeCount >= requestedCount) {
    // summaries.add(new AssignmentSummary(level, requestedCount, activeCount, 0));
    // responseDetails.addAll(activeForLevel);
    // continue;
    // }
    //
    // // Missing slots to assign
    // int missing = requestedCount - activeCount;
    //
    // // 2) All previous assignments (any status) for reuse logic
    // List<UserGuardAssignment> previous =
    // assignmentRepository.findByCategoryAndGuardLevel(category, level);
    //
    // // 3) Active guard ids (to exclude)
    // Set<Long> activeGuardIds = previous.stream()
    // .filter(a -> "Active".equalsIgnoreCase(a.getStatus()))
    // .map(a -> a.getOfficer().getId())
    // .collect(Collectors.toSet());
    //
    // // 4) Inactive previous assignments (potentially reusable)
    // List<UserGuardAssignment> inactivePrevious = previous.stream()
    // .filter(a -> !"Active".equalsIgnoreCase(a.getStatus()))
    // .collect(Collectors.toList());
    //
    // // 5) Officers of required level currently Inactive
    // List<Officer> allGuardsOfLevel = officerRepository.findByRank(level).stream()
    // .filter(o -> "Inactive".equalsIgnoreCase(o.getStatus()))
    // .collect(Collectors.toList());
    //
    // // 6) Exclude those already active in other assignments for this category
    // List<Officer> availableGuards = allGuardsOfLevel.stream()
    // .filter(o -> !activeGuardIds.contains(o.getId()))
    // .collect(Collectors.toList());
    //
    // // 7) Build timesAssigned map and exclude those reaching MAX_REUSE
    // Map<Long, Integer> timesMap = inactivePrevious.stream()
    // .collect(Collectors.toMap(
    // a -> a.getOfficer().getId(),
    // UserGuardAssignment::getTimesAssigned,
    // (oldV, newV) -> Math.max(oldV, newV) // merge duplicates
    // ));
    //
    // Set<Long> excludedIds = timesMap.entrySet().stream()
    // .filter(e -> e.getValue() >= MAX_REUSE)
    // .map(Map.Entry::getKey)
    // .collect(Collectors.toSet());
    //
    // List<Officer> cleanedAvailable = availableGuards.stream()
    // .filter(o -> !excludedIds.contains(o.getId()))
    // .collect(Collectors.toList());
    //
    // // 8) Reusable officers from previous assignments
    // List<Officer> reusable = inactivePrevious.stream()
    // .filter(a -> a.getTimesAssigned() < MAX_REUSE)
    // .map(UserGuardAssignment::getOfficer)
    // .filter(o -> !excludedIds.contains(o.getId()))
    // .collect(Collectors.toList());
    //
    // // 9) Combine unique (available + reusable)
    // Map<Long, Officer> combinedMap = new LinkedHashMap<>();
    // cleanedAvailable.forEach(o -> combinedMap.put(o.getId(), o));
    // reusable.forEach(o -> combinedMap.putIfAbsent(o.getId(), o));
    // List<Officer> combinedPool = new ArrayList<>(combinedMap.values());
    //
    // // 10) Select randomly up to missing
    // Collections.shuffle(combinedPool);
    // int willAssign = Math.min(missing, combinedPool.size());
    // List<Officer> selected = combinedPool.subList(0, willAssign);
    //
    // // 11) Create/update assignments for selected officers
    // List<UserGuardAssignment> createdOrUpdated = new ArrayList<>();
    // for (Officer officer : selected) {
    // // mark officer active immediately (reduces race but not perfect)
    // officer.setStatus("Active");
    // officerRepository.save(officer);
    //
    // Optional<UserGuardAssignment> existing = inactivePrevious.stream()
    // .filter(a -> a.getOfficer().getId().equals(officer.getId()))
    // .findFirst();
    //
    // UserGuardAssignment assignment;
    // if (existing.isPresent()) {
    // assignment = existing.get();
    // assignment.setTimesAssigned(Optional.ofNullable(assignment.getTimesAssigned()).orElse(0)
    // + 1);
    // } else {
    // assignment = new UserGuardAssignment();
    // assignment.setCategory(category);
    // assignment.setOfficer(officer);
    // assignment.setTimesAssigned(1);
    // }
    // assignment.setStatus("Active");
    // assignment.setAssignedAt(LocalDateTime.now());
    // assignment.setStartAt(request.getStartAt());
    // assignment.setEndAt(request.getEndAt());
    // createdOrUpdated.add(assignment);
    // System.out.println(assignment.getAtEnd()+"atend");
    // System.out.println(assignment.getStartAt() + "startat ");
    // System.out.println(assignment.getEndAt() + "endat");
    //
    // }
    // Optional<Category> maybe = categoryRepository.findById(request.getUserId());
    // if (maybe.isPresent()) {
    // Category officer = maybe.get();
    // officer.setStatus("Active"); // or "Removed" depending on your domain
    // categoryRepository.save(officer);
    // }
    //
    // // 12) Persist new/updated assignments and collect results
    // if (!createdOrUpdated.isEmpty()) {
    // List<UserGuardAssignment> saved =
    // assignmentRepository.saveAll(createdOrUpdated);
    // responseDetails.addAll(saved);
    // }
    //
    // // include previously active so response contains all current active
    // assignments
    // // for this level
    // responseDetails.addAll(activeForLevel);
    //
    // int totalAssignedNow = activeCount + willAssign;
    // int nowMissing = Math.max(0, requestedCount - totalAssignedNow);
    // summaries.add(new AssignmentSummary(level, requestedCount, totalAssignedNow,
    // nowMissing));
    // }
    //
    // AssignmentResponse resp = new AssignmentResponse();
    // resp.setSummary(summaries);
    // resp.setDetails(responseDetails);
    // return resp;
    // }

    /**
     * Read endpoint: returns current active assignments grouped by level and the
     * list of active details.
     */

    @Transactional
    public AssignmentResponse assignGuardsAutomatically(GuardAssignmentRequest request) {
        Category category = categoryRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Category (user) not found"));

        List<UserGuardAssignment> responseDetails = new ArrayList<>();
        List<AssignmentSummary> summaries = new ArrayList<>();

        // Map level -> selected officers to assign (dry run results)
        Map<String, List<Officer>> selectionsByLevel = new LinkedHashMap<>();
        Map<String, Integer> requestedByLevel = new HashMap<>();

        // ----- DRY RUN: determine candidates for each level without saving -----
        for (GuardLevelRequest levelReq : request.getLevels()) {
            String level = levelReq.getGuardLevel();
            int requestedCount = levelReq.getNumberOfGuards();
            requestedByLevel.put(level, requestedCount);

            // 1) Current active assignments for this category+level
            List<UserGuardAssignment> activeForLevel = assignmentRepository
                    .findByCategoryAndGuardLevelAndStatus(category, level, "Active");
            int activeCount = activeForLevel.size();

            if (activeCount >= requestedCount) {
                // nothing to assign for this level
                summaries.add(new AssignmentSummary(level, requestedCount, activeCount, 0));
                selectionsByLevel.put(level, Collections.emptyList());
                continue;
            }

            int missing = requestedCount - activeCount;

            // 2) All previous assignments (any status) for reuse logic
            List<UserGuardAssignment> previous = assignmentRepository.findByCategoryAndGuardLevel(category, level);

            // 3) Active guard ids (to exclude)
            Set<Long> activeGuardIds = previous.stream()
                    .filter(a -> "Active".equalsIgnoreCase(a.getStatus()))
                    .map(a -> a.getOfficer().getId())
                    .collect(Collectors.toSet());

            // 4) Inactive previous assignments (potentially reusable)
            List<UserGuardAssignment> inactivePrevious = previous.stream()
                    .filter(a -> !"Active".equalsIgnoreCase(a.getStatus()))
                    .collect(Collectors.toList());

            // 5) Officers of required level currently Inactive
            List<Officer> allGuardsOfLevel = officerRepository.findByRank(level).stream()
                    .filter(o -> "Inactive".equalsIgnoreCase(o.getStatus()))
                    .collect(Collectors.toList());

            // 6) Exclude those already active in other assignments for this category
            List<Officer> availableGuards = allGuardsOfLevel.stream()
                    .filter(o -> !activeGuardIds.contains(o.getId()))
                    .collect(Collectors.toList());

            // 7) Build timesAssigned map and exclude those reaching MAX_REUSE
            Map<Long, Integer> timesMap = inactivePrevious.stream()
                    .collect(Collectors.toMap(
                            a -> a.getOfficer().getId(),
                            UserGuardAssignment::getTimesAssigned,
                            (oldV, newV) -> Math.max(oldV, newV)));

            Set<Long> excludedIds = timesMap.entrySet().stream()
                    .filter(e -> e.getValue() >= MAX_REUSE)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            List<Officer> cleanedAvailable = availableGuards.stream()
                    .filter(o -> !excludedIds.contains(o.getId()))
                    .collect(Collectors.toList());

            // 8) Reusable officers from previous assignments
            List<Officer> reusable = inactivePrevious.stream()
                    .filter(a -> a.getTimesAssigned() < MAX_REUSE)
                    .map(UserGuardAssignment::getOfficer)
                    .filter(o -> !excludedIds.contains(o.getId()))
                    .collect(Collectors.toList());

            // 9) Combine unique (available + reusable)
            Map<Long, Officer> combinedMap = new LinkedHashMap<>();
            cleanedAvailable.forEach(o -> combinedMap.put(o.getId(), o));
            reusable.forEach(o -> combinedMap.putIfAbsent(o.getId(), o));
            List<Officer> combinedPool = new ArrayList<>(combinedMap.values());

            // 10) Select randomly up to missing (dry run)
            Collections.shuffle(combinedPool);
            int willAssign = Math.min(missing, combinedPool.size());
            List<Officer> selected = combinedPool.subList(0, willAssign);

            // store dry-run selection and summary data
            selectionsByLevel.put(level, new ArrayList<>(selected));
            int totalAssignedNow = activeCount + willAssign;
            int nowMissing = Math.max(0, requestedCount - totalAssignedNow);
            summaries.add(new AssignmentSummary(level, requestedCount, totalAssignedNow, nowMissing));
        }

        // ----- VALIDATION: if any level still misses, abort (no assignment) -----
        List<String> failingLevels = summaries.stream()
                .filter(s -> s.getMissing() > 0)
                .map(AssignmentSummary::getLevel)
                .collect(Collectors.toList());

        if (!failingLevels.isEmpty()) {
            // Throwing a runtime exception will roll back (because @Transactional).
            // Provide a helpful message so caller knows which levels failed.
            throw new RuntimeException("Insufficient guards for levels: " + String.join(", ", failingLevels) +
                    ". No assignments were created.");
        }

        // ----- COMMIT PHASE: persist all assignments (safe: every level satisfied)
        // -----
        for (Map.Entry<String, List<Officer>> entry : selectionsByLevel.entrySet()) {
            String level = entry.getKey();
            List<Officer> selected = entry.getValue();
            if (selected.isEmpty()) {
                // nothing to create for this level (already satisfied)
                // also add existing active assignments to response details
                List<UserGuardAssignment> activeForLevel = assignmentRepository
                        .findByCategoryAndGuardLevelAndStatus(category, level, "Active");
                responseDetails.addAll(activeForLevel);
                continue;
            }

            // fetch previous inactive assignments once for this level
            List<UserGuardAssignment> previous = assignmentRepository.findByCategoryAndGuardLevel(category, level);
            List<UserGuardAssignment> inactivePrevious = previous.stream()
                    .filter(a -> !"Active".equalsIgnoreCase(a.getStatus()))
                    .collect(Collectors.toList());

            List<UserGuardAssignment> createdOrUpdated = new ArrayList<>();
            for (Officer officer : selected) {
                // mark officer active immediately
                officer.setStatus("Active");
                officerRepository.save(officer);

                Optional<UserGuardAssignment> existing = inactivePrevious.stream()
                        .filter(a -> a.getOfficer().getId().equals(officer.getId()))
                        .findFirst();

                UserGuardAssignment assignment;
                if (existing.isPresent()) {
                    assignment = existing.get();
                    assignment.setTimesAssigned(Optional.ofNullable(assignment.getTimesAssigned()).orElse(0) + 1);
                } else {
                    assignment = new UserGuardAssignment();
                    assignment.setCategory(category);
                    assignment.setOfficer(officer);
                    assignment.setTimesAssigned(1);
                }
                assignment.setStatus("Active");
                assignment.setAssignedAt(LocalDateTime.now());
                assignment.setStartAt(request.getStartAt());
                assignment.setEndAt(request.getEndAt());
                createdOrUpdated.add(assignment);
            }

            if (!createdOrUpdated.isEmpty()) {
                List<UserGuardAssignment> saved = assignmentRepository.saveAll(createdOrUpdated);
                responseDetails.addAll(saved);
            }
            // include previously active so response contains all current active assignments
            // for this level
            List<UserGuardAssignment> activeForLevel = assignmentRepository
                    .findByCategoryAndGuardLevelAndStatus(category, level, "Active");
            responseDetails.addAll(activeForLevel);
        }

        // mark category active if any assignments were created
        if (!responseDetails.isEmpty()) {
            category.setStatus("Active");
            categoryRepository.save(category);
        }

        AssignmentResponse resp = new AssignmentResponse();
        resp.setSummary(summaries);
        resp.setDetails(responseDetails);
        return resp;
    }

    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentResponseForCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found for id: " + categoryId));

        // Use JPQL join fetch to load officer together with assignments
        String jpql = "select a from UserGuardAssignment a left join fetch a.officer where a.category.id = :catId";
        TypedQuery<UserGuardAssignment> q = em.createQuery(jpql, UserGuardAssignment.class);
        q.setParameter("catId", category.getId());
        List<UserGuardAssignment> allForCategory = q.getResultList();

        // Debug prints
        System.out.println("Category fetched: " + category);
        System.out.println("total assignments for category " + category.getId() + ": "
                + (allForCategory == null ? 0 : allForCategory.size()));
        if (allForCategory != null) {
            allForCategory.forEach(a -> System.out.println("assignId=" + a.getId()
                    + " status=" + a.getStatus()
                    + " officer="
                    + (a.getOfficer() == null ? "null" : a.getOfficer().getId() + ":" + a.getOfficer().getRank())));
        }

        // Build summary of Active assignments grouped by officer.rank
        Map<String, Long> assignedByLevel = Optional.ofNullable(allForCategory).orElseGet(Collections::emptyList)
                .stream()
                .filter(a -> "Active".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.groupingBy(
                        a -> (a.getOfficer() == null || a.getOfficer().getRank() == null) ? "UNASSIGNED"
                                : a.getOfficer().getRank(),
                        Collectors.counting()));

        List<AssignmentSummary> summaries = assignedByLevel.entrySet().stream()
                .map(e -> new AssignmentSummary(e.getKey(), 0, e.getValue().intValue(), 0))
                .collect(Collectors.toList());

        AssignmentResponse resp = new AssignmentResponse();
        resp.setSummary(summaries);

        // Details: only active assignments (change this if you want both Active +
        // Inactive)
        List<UserGuardAssignment> details = Optional.ofNullable(allForCategory).orElseGet(Collections::emptyList)
                .stream()
                .filter(a -> "Active".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.toList());
        resp.setDetails(details);

        System.out.println("Returning AssignmentResponse: " + resp);
        return resp;
    }

    @Transactional(readOnly = true)
    public Category getVipForGuard(Long officerId) {
        UserGuardAssignment assignment = assignmentRepository
                .findFirstByOfficerIdAndStatusOrderByAssignedAtDesc(officerId, "Active")
                .orElseThrow(() -> new RuntimeException(
                        "No active VIP assignment found for guard id: " + officerId));

        return assignment.getCategory(); // this is the VIP
    }
}
