// package com.infotech.service;
//
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Objects;
// import java.util.Optional;
// import java.util.function.BiConsumer;
//
// import com.infotech.dto.OfficerRequestDto;
// import com.infotech.dto.OfficerResponseDto;
// import com.infotech.entity.HistoryManagement;
// import com.infotech.entity.Officer;
// import com.infotech.exception.BadRequestException;
// import com.infotech.exception.ResourceNotFoundException;
// import com.infotech.repository.HistoryManagementRepository;
// import com.infotech.repository.OfficerRepository;
//
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Service;
//
// import lombok.RequiredArgsConstructor;
//
// @Service
// @RequiredArgsConstructor
// public class OfficerService {
//
//     private static final String STATUS_ACTIVE = "Inactive";
//     private static final String STATUS_DELETED = "Deleted";
//
//     private final OfficerRepository officerRepository;
//     private final HistoryManagementRepository historyManagementRepository;
//     private final PasswordEncoder encoder;
//
//     // ─────────────── MAPPERS ───────────────
//
//     private OfficerResponseDto toDto(Officer officer) {
//         OfficerResponseDto dto = new OfficerResponseDto();
//         dto.setId(officer.getId());
//         dto.setName(officer.getName());
//         dto.setUsername(officer.getUsername());
//         dto.setEmail(officer.getEmail());
//         dto.setRank(officer.getRank());
//         dto.setStatus(officer.getStatus());
//         dto.setExperience(officer.getExperience());
//         dto.setContactno(officer.getContactno());
//         return dto;
//     }
//
//     private Officer fromCreateDto(OfficerRequestDto dto) {
//         Officer o = new Officer();
//         o.setName(dto.getName());
//         o.setUsername(dto.getUsername());
//         o.setEmail(dto.getEmail());
//         o.setRank(dto.getRank());
//         o.setStatus(dto.getStatus()); // may be null; service sets default
//         o.setExperience(dto.getExperience());
//         o.setContactno(dto.getContactno());
//         o.setPassword(dto.getPassword());
//         return o;
//     }
//
//     // ─────────────── READ ───────────────
//
//     public Page<OfficerResponseDto> getOfficers(int page, int size, String status, String rank) {
//         Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
//
//         Page<Officer> officersPage;
//
//         if (status != null && !status.isBlank() && rank != null && !rank.isBlank()) {
//             officersPage = officerRepository.findByStatusAndRank(status, rank, pageable);
//         } else if (status != null && !status.isBlank()) {
//             officersPage = officerRepository.findByStatus(status, pageable);
//         } else if (rank != null && !rank.isBlank()) {
//             officersPage = officerRepository.findByRank(rank, pageable);
//         } else {
//             officersPage = officerRepository.findAll(pageable);
//         }
//
//         return officersPage.map(this::toDto);
//     }
//
//     public Optional<OfficerResponseDto> getByUsername(String username) {
//         return officerRepository.findByUsername(username).map(this::toDto);
//     }
//
//     public Officer getByIdOrThrow(Long id) {
//         return officerRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("Officer not found with id " + id));
//     }
//
//     public OfficerResponseDto getByIdDto(Long id) {
//         return toDto(getByIdOrThrow(id));
//     }
//
//     // ─────────────── CREATE / RESTORE ───────────────
//
//     public OfficerResponseDto createOrRestoreOfficer(OfficerRequestDto officerDto, String operatedBy) {
//
//         if (officerDto.getUsername() == null || officerDto.getUsername().isBlank()) {
//             throw new BadRequestException("Username is required");
//         }
//         if (officerDto.getPassword() == null || officerDto.getPassword().isBlank()) {
//             throw new BadRequestException("Password is required");
//         }
//
//         Officer officer = fromCreateDto(officerDto);
//
//         // Try to restore using soft-deleted matches
//         Optional<Officer> softDeletedOpt = findSoftDeletedMatch(officer);
//
//         if (softDeletedOpt.isPresent()) {
//             Officer deleted = softDeletedOpt.get();
//             String oldStatus = deleted.getStatus();
//
//             deleted.setStatus(STATUS_ACTIVE);
//             deleted.setName(officer.getName());
//             deleted.setEmail(officer.getEmail());
//             deleted.setRank(officer.getRank());
//             deleted.setExperience(officer.getExperience());
//             deleted.setContactno(officer.getContactno());
//             deleted.setUsername(officer.getUsername());
//             deleted.setPassword(encoder.encode(officer.getPassword()));
//
//             Officer restored = officerRepository.save(deleted);
//
//             // history: RESTORE
//             logHistory("RESTORE", "Officer", restored.getId(), operatedBy,
//                     "status", oldStatus, STATUS_ACTIVE);
//
//             return toDto(restored);
//         }
//
//         // No soft-deleted match → normal create
//         officer.setStatus(STATUS_ACTIVE);
//         officer.setPassword(encoder.encode(officer.getPassword()));
//
//         Officer saved = officerRepository.save(officer);
//
//         // history: CREATE
//         logHistory("CREATE", "Officer", saved.getId(), operatedBy,
//                 "status", null, STATUS_ACTIVE);
//
//         return toDto(saved);
//     }
//
//     /**
//      * Find a soft-deleted Officer that matches by username OR email OR contactno.
//      */
//     private Optional<Officer> findSoftDeletedMatch(Officer officer) {
//         List<Officer> candidates = new ArrayList<>();
//
//         if (officer.getUsername() != null && !officer.getUsername().isBlank()) {
//             officerRepository.findByUsernameAndStatus(officer.getUsername(), STATUS_DELETED)
//                     .ifPresent(candidates::add);
//         }
//
//         if (officer.getEmail() != null && !officer.getEmail().isBlank()) {
//             officerRepository.findByEmailAndStatus(officer.getEmail(), STATUS_DELETED)
//                     .ifPresent(candidates::add);
//         }
//
//         if (officer.getContactno() != null) {
//             officerRepository.findByContactnoAndStatus(officer.getContactno(), STATUS_DELETED)
//                     .ifPresent(candidates::add);
//         }
//
//         if (candidates.isEmpty()) {
//             return Optional.empty();
//         }
//
//         // If multiple match, you can improve this selection logic
//         return Optional.of(candidates.get(0));
//     }
//
//     // ─────────────── UPDATE ───────────────
//
//     public OfficerResponseDto updateOfficer(Long id, OfficerRequestDto updatedDto, String operatedBy) {
//
//         Officer officer = officerRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("Officer not found with id " + id));
//
//         List<HistoryManagement> historyEntries = new ArrayList<>();
//
//         // helper: record change for any type
//         BiConsumer<String, Object[]> logChange = (fieldName, values) -> {
//             Object oldVal = values[0];
//             Object newVal = values[1];
//
//             if (Objects.equals(oldVal, newVal)) {
//                 return;
//             }
//
//             HistoryManagement h = new HistoryManagement();
//             h.setTime(LocalDateTime.now());
//             h.setOperationType("UPDATE");
//             h.setOperatedBy(operatedBy);
//             h.setOperatorId(id);
//             h.setEntityName("Officer");
//             h.setFieldName(fieldName);
//             h.setOldValue(oldVal == null ? null : oldVal.toString());
//             h.setNewValue(newVal == null ? null : newVal.toString());
//
//             historyEntries.add(h);
//         };
//
//         // name
//         logChange.accept("name", new Object[] {
//                 officer.getName(),
//                 updatedDto.getName()
//         });
//         officer.setName(updatedDto.getName());
//
//         // email
//         logChange.accept("email", new Object[] {
//                 officer.getEmail(),
//                 updatedDto.getEmail()
//         });
//         officer.setEmail(updatedDto.getEmail());
//
//         // rank
//         logChange.accept("rank", new Object[] {
//                 officer.getRank(),
//                 updatedDto.getRank()
//         });
//         officer.setRank(updatedDto.getRank());
//
//         // status (optional in DTO)
//         if (updatedDto.getStatus() != null) {
//             logChange.accept("status", new Object[] {
//                     officer.getStatus(),
//                     updatedDto.getStatus()
//             });
//             officer.setStatus(updatedDto.getStatus());
//         }
//
//         // experience
//         logChange.accept("experience", new Object[] {
//                 officer.getExperience(),
//                 updatedDto.getExperience()
//         });
//         officer.setExperience(updatedDto.getExperience());
//
//         // contactno
//         logChange.accept("contactno", new Object[] {
//                 officer.getContactno(),
//                 updatedDto.getContactno()
//         });
//         officer.setContactno(updatedDto.getContactno());
//
//         // username
//         logChange.accept("username", new Object[] {
//                 officer.getUsername(),
//                 updatedDto.getUsername()
//         });
//         officer.setUsername(updatedDto.getUsername());
//
//         // password (only if provided & changed)
//         String newRawPassword = updatedDto.getPassword();
//         if (newRawPassword != null && !newRawPassword.isBlank()) {
//
//             boolean sameAsOld = encoder.matches(newRawPassword, officer.getPassword());
//
//             if (!sameAsOld) {
//                 HistoryManagement h = new HistoryManagement();
//                 h.setTime(LocalDateTime.now());
//                 h.setOperationType("UPDATE");
//                 h.setOperatedBy(operatedBy);
//                 h.setOperatorId(id);
//                 h.setEntityName("Officer");
//                 h.setFieldName("password");
//                 h.setOldValue(null);
//                 h.setNewValue("UPDATED");
//                 historyEntries.add(h);
//
//                 officer.setPassword(encoder.encode(newRawPassword));
//             }
//         }
//
//         Officer saved = officerRepository.save(officer);
//
//         if (!historyEntries.isEmpty()) {
//             historyManagementRepository.saveAll(historyEntries);
//         }
//
//         return toDto(saved);
//     }
//
//     // ─────────────── DELETE (SOFT) ───────────────
//
//     public void softDeleteOfficer(Long id, String operatedBy) {
//         Officer officer = officerRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("Officer not found with id " + id));
//
//         String oldStatus = officer.getStatus();
//
//         officer.setStatus(STATUS_DELETED);
//         officerRepository.save(officer);
//
//         logHistory("DELETE", "Officer", id, operatedBy,
//                 "status", oldStatus, STATUS_DELETED);
//     }
//
//     // ─────────────── HISTORY HELPER ───────────────
//
//     private void logHistory(String operationType,
//             String entityName,
//             Long operatorId,
//             String operatedBy,
//             String fieldName,
//             String oldValue,
//             String newValue) {
//
//         HistoryManagement history = new HistoryManagement();
//         history.setTime(LocalDateTime.now());
//         history.setOperationType(operationType);
//         history.setOperatedBy(operatedBy);
//         history.setOperatorId(operatorId);
//         history.setEntityName(entityName);
//         history.setFieldName(fieldName);
//         history.setOldValue(oldValue);
//         history.setNewValue(newValue);
//
//         historyManagementRepository.save(history);
//     }
// }
//
//
//
//
//
//
//
//
//
//
//
//
//

package com.infotech.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.infotech.dto.OfficerRequestDto;
import com.infotech.dto.OfficerResponseDto;
import com.infotech.entity.HistoryManagement;
import com.infotech.entity.NotificationManagement;
import com.infotech.entity.Officer;
import com.infotech.exception.BadRequestException;
import com.infotech.exception.ResourceNotFoundException;
import com.infotech.repository.HistoryManagementRepository;
import com.infotech.repository.NotificationManagementRepository;
import com.infotech.repository.OfficerRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfficerService {

  // NOTE: kept exactly as you have it
  private static final String STATUS_ACTIVE = "Inactive";
  private static final String STATUS_DELETED = "Deleted";

  private final OfficerRepository officerRepository;
  private final HistoryManagementRepository historyManagementRepository;
  private final PasswordEncoder encoder;
  private final NotificationManagementRepository notificationManagementRepository;
  private final FcmService service;

  // ─────────────── MAPPERS ───────────────

  private OfficerResponseDto toDto(Officer officer) {
    OfficerResponseDto dto = new OfficerResponseDto();
    dto.setId(officer.getId());
    dto.setName(officer.getName());
    dto.setUsername(officer.getUsername());
    dto.setEmail(officer.getEmail());
    dto.setRank(officer.getRank());
    dto.setStatus(officer.getStatus());
    dto.setExperience(officer.getExperience());
    dto.setContactno(officer.getContactno());
    return dto;
  }

  private Officer fromCreateDto(OfficerRequestDto dto) {
    Officer o = new Officer();
    o.setName(dto.getName());
    o.setUsername(dto.getUsername());
    o.setEmail(dto.getEmail());
    o.setRank(dto.getRank());
    o.setStatus(dto.getStatus()); // may be null; service sets default
    o.setExperience(dto.getExperience());
    o.setContactno(dto.getContactno());
    o.setPassword(dto.getPassword());

    return o;
  }

  // ─────────────── READ ───────────────

  public Page<OfficerResponseDto> getOfficers(int page, int size, String status, String rank) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

    Page<Officer> officersPage;

    if (status != null && !status.isBlank() && rank != null && !rank.isBlank()) {
      officersPage = officerRepository.findByStatusAndRank(status, rank, pageable);
    } else if (status != null && !status.isBlank()) {
      officersPage = officerRepository.findByStatus(status, pageable);
    } else if (rank != null && !rank.isBlank()) {
      officersPage = officerRepository.findByRank(rank, pageable);
    } else {
      officersPage = officerRepository.findAll(pageable);
    }

    return officersPage.map(this::toDto);
  }

  public Optional<OfficerResponseDto> getByUsername(String username) {
    return officerRepository.findByUsername(username).map(this::toDto);
  }

  public Officer getByIdOrThrow(Long id) {
    return officerRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Officer not found with id " + id));
  }

  public OfficerResponseDto getByIdDto(Long id) {
    return toDto(getByIdOrThrow(id));
  }

  // ─────────────── UNIQUE FIELD VALIDATION ───────────────
  /**
   * Validates that username, email and contactno are unique among all
   * NON-DELETED officers.
   *
   * @param dto      request DTO (create/update)
   * @param ignoreId officer id to ignore (for update), or null for create.
   * @throws BadRequestException if any field already exists.
   */
  private void validateUniqueFields(OfficerRequestDto dto, Long ignoreId) {

    // username
    if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
      officerRepository.findByUsernameAndStatusNot(dto.getUsername(), STATUS_DELETED)
          .filter(o -> !Objects.equals(o.getId(), ignoreId))
          .ifPresent(o -> {
            throw new BadRequestException("Username already exists");
          });
    }

    // email
    if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
      officerRepository.findByEmailAndStatusNot(dto.getEmail(), STATUS_DELETED)
          .filter(o -> !Objects.equals(o.getId(), ignoreId))
          .ifPresent(o -> {
            throw new BadRequestException("Email already exists");
          });
    }

    // contactno
    if (dto.getContactno() != null) {
      officerRepository.findByContactnoAndStatusNot(dto.getContactno(), STATUS_DELETED)
          .filter(o -> !Objects.equals(o.getId(), ignoreId))
          .ifPresent(o -> {
            throw new BadRequestException("Contact number already exists");
          });
    }
  }

  // ─────────────── CREATE / RESTORE ───────────────

  public OfficerResponseDto createOrRestoreOfficer(OfficerRequestDto officerDto, String operatedBy) {

    if (officerDto.getUsername() == null || officerDto.getUsername().isBlank()) {
      throw new BadRequestException("Username is required");
    }
    if (officerDto.getPassword() == null || officerDto.getPassword().isBlank()) {
      throw new BadRequestException("Password is required");
    }
    if (officerDto.getEmail() == null || officerDto.getEmail().isBlank()) {
      throw new BadRequestException("Email is required");
    }

    if (officerDto.getName() == null || officerDto.getName().isBlank()) {
      throw new BadRequestException("Name is required");
    }
    if (officerDto.getRank() == null || officerDto.getRank().isBlank()) {
      throw new BadRequestException("Rank is required");
    }
    if (officerDto.getStatus() == null || officerDto.getStatus().isBlank()) {
      throw new BadRequestException("Rank is required");
    }
    if (officerDto.getContactno() == null) {
      throw new BadRequestException("Contact No is required");
    }

    // uniqueness check for create (ignoreId = null)
    validateUniqueFields(officerDto, null);

    Officer officer = fromCreateDto(officerDto);

    // Try to restore using soft-deleted matches
    Optional<Officer> softDeletedOpt = findSoftDeletedMatch(officer);

    if (softDeletedOpt.isPresent()) {
      Officer deleted = softDeletedOpt.get();
      String oldStatus = deleted.getStatus();

      deleted.setStatus(STATUS_ACTIVE);
      deleted.setName(officer.getName());
      deleted.setEmail(officer.getEmail());
      deleted.setRank(officer.getRank());
      deleted.setExperience(officer.getExperience());
      deleted.setContactno(officer.getContactno());
      deleted.setUsername(officer.getUsername());
      deleted.setPassword(encoder.encode(officer.getPassword()));

      Officer restored = officerRepository.save(deleted);

      // history: RESTORE
      logHistory("RESTORE", "GUARD", restored.getId(), operatedBy,
          "status", oldStatus, STATUS_ACTIVE);

      return toDto(restored);
    }

    // No soft-deleted match → normal create
    officer.setPassword(encoder.encode(officer.getPassword()));

    Officer saved = officerRepository.save(officer);

    // history: CREATE
    logHistory("CREATE", "GUARD", saved.getId(), operatedBy,
        "status", null, STATUS_ACTIVE);
    if (officerDto.getStatus().equals("self")) {
      // Log DELETE
      //
      //
      //
      NotificationManagement existingNotificationAdmin = notificationManagementRepository
          .findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(1L, "admin");
      String adminFcmToken = existingNotificationAdmin != null
          ? existingNotificationAdmin.getNotificationToken()
          : null;
      Long adminId = existingNotificationAdmin != null
          ? existingNotificationAdmin.getNotificationSenderId()
          : null;

      service.sendNotificationSafely(
          adminFcmToken,
          "New Guard Self Registration",
          "New Guard Self Registration Just Happen Please Update Its Status",
          "admin",
          adminId);

      NotificationManagement notification = new NotificationManagement();
      notification.setNotificationSenderId(officer.getId());
      notification.setNotificationSender("Self Registration");
      notification.setNotificationSenderName(officerDto.getName());
      notification.setNotificationMessage("New Guard Self Registration Just Happen Please Check Its Status");
      notification.setNotificationStatus(false);
      notification.setNotificationAssignTime(LocalDateTime.now());
      notificationManagementRepository.save(notification);

      logHistory("DELETE", "vip", saved.getId(), operatedBy,
          "status", STATUS_ACTIVE, STATUS_DELETED);
    }

    return toDto(saved);
  }

  /**
   * Find a soft-deleted Officer that matches by username OR email OR contactno.
   */
  private Optional<Officer> findSoftDeletedMatch(Officer officer) {
    List<Officer> candidates = new ArrayList<>();

    if (officer.getUsername() != null && !officer.getUsername().isBlank()) {
      officerRepository.findByUsernameAndStatus(officer.getUsername(), STATUS_DELETED)
          .ifPresent(candidates::add);
    }

    if (officer.getEmail() != null && !officer.getEmail().isBlank()) {
      officerRepository.findByEmailAndStatus(officer.getEmail(), STATUS_DELETED)
          .ifPresent(candidates::add);
    }

    if (officer.getContactno() != null) {
      officerRepository.findByContactnoAndStatus(officer.getContactno(), STATUS_DELETED)
          .ifPresent(candidates::add);
    }

    if (candidates.isEmpty()) {
      return Optional.empty();
    }

    // If multiple match, you can improve this selection logic
    return Optional.of(candidates.get(0));
  }

  // ─────────────── UPDATE ───────────────

  // ─────────────── UPDATE ───────────────

  public OfficerResponseDto updateOfficer(Long id, OfficerRequestDto updatedDto, String operatedBy) {

    // basic null check – safer error instead of NPE
    if (updatedDto == null) {
      throw new BadRequestException("Request body cannot be null");
    }

    Officer officer = officerRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Officer not found with id " + id));

    // uniqueness check for update (ignore this officer's own id)
    validateUniqueFields(updatedDto, id);

    List<HistoryManagement> historyEntries = new ArrayList<>();

    // helper: record change for any type
    BiConsumer<String, Object[]> logChange = (fieldName, values) -> {
      Object oldVal = values[0];
      Object newVal = values[1];

      // if same or both null → no history
      if (Objects.equals(oldVal, newVal)) {
        return;
      }

      HistoryManagement h = new HistoryManagement();
      h.setTime(LocalDateTime.now());
      h.setOperationType("UPDATE");
      h.setOperatedBy(operatedBy);
      h.setOperatorId(id);
      h.setEntityName("Guard");
      h.setFieldName(fieldName);
      h.setOldValue(oldVal == null ? null : oldVal.toString());
      h.setNewValue(newVal == null ? null : newVal.toString());

      historyEntries.add(h);
    };

    // name (only update if provided)
    if (updatedDto.getName() != null && !updatedDto.getName().isBlank()) {
      logChange.accept("name", new Object[] {
          officer.getName(),
          updatedDto.getName()
      });
      officer.setName(updatedDto.getName());
    }

    // email
    if (updatedDto.getEmail() != null && !updatedDto.getEmail().isBlank()) {
      logChange.accept("email", new Object[] {
          officer.getEmail(),
          updatedDto.getEmail()
      });
      officer.setEmail(updatedDto.getEmail());
    }

    // rank
    if (updatedDto.getRank() != null && !updatedDto.getRank().isBlank()) {
      logChange.accept("rank", new Object[] {
          officer.getRank(),
          updatedDto.getRank()
      });
      officer.setRank(updatedDto.getRank());
    }

    // status (optional in DTO)
    if (updatedDto.getStatus() != null && !updatedDto.getStatus().isBlank()) {
      logChange.accept("status", new Object[] {
          officer.getStatus(),
          updatedDto.getStatus()
      });
      officer.setStatus(updatedDto.getStatus());
    }

    // experience (only if not null – avoids overwriting with null)
    if (updatedDto.getExperience() != null) {
      logChange.accept("experience", new Object[] {
          officer.getExperience(),
          updatedDto.getExperience()
      });
      officer.setExperience(updatedDto.getExperience());
    }

    // contactno
    if (updatedDto.getContactno() != null) {
      logChange.accept("contactno", new Object[] {
          officer.getContactno(),
          updatedDto.getContactno()
      });
      officer.setContactno(updatedDto.getContactno());
    }

    // username
    if (updatedDto.getUsername() != null && !updatedDto.getUsername().isBlank()) {
      logChange.accept("username", new Object[] {
          officer.getUsername(),
          updatedDto.getUsername()
      });
      officer.setUsername(updatedDto.getUsername());
    }

    // password (only if provided & changed)
    String newRawPassword = updatedDto.getPassword();
    if (newRawPassword != null && !newRawPassword.isBlank()) {

      boolean sameAsOld = encoder.matches(newRawPassword, officer.getPassword());

      if (!sameAsOld) {
        HistoryManagement h = new HistoryManagement();
        h.setTime(LocalDateTime.now());
        h.setOperationType("UPDATE");
        h.setOperatedBy(operatedBy);
        h.setOperatorId(id);
        h.setEntityName("GUARD");
        h.setFieldName("password");
        h.setOldValue(null);
        h.setNewValue("UPDATED");
        historyEntries.add(h);

        officer.setPassword(encoder.encode(newRawPassword));
      }
    }

    Officer saved = officerRepository.save(officer);

    if (!historyEntries.isEmpty()) {
      historyManagementRepository.saveAll(historyEntries);
    }

    return toDto(saved);
  }
  // ─────────────── DELETE (SOFT) ───────────────

  public void softDeleteOfficer(Long id, String operatedBy) {
    Officer officer = officerRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Officer not found with id " + id));

    String oldStatus = officer.getStatus();

    officer.setStatus(STATUS_DELETED);
    officerRepository.save(officer);

    logHistory("DELETE", "GUARD", id, operatedBy,
        "status", oldStatus, STATUS_DELETED);
  }

  // ─────────────── HISTORY HELPER ───────────────

  private void logHistory(String operationType,
      String entityName,
      Long operatorId,
      String operatedBy,
      String fieldName,
      String oldValue,
      String newValue) {

    HistoryManagement history = new HistoryManagement();
    history.setTime(LocalDateTime.now());
    history.setOperationType(operationType);
    history.setOperatedBy(operatedBy);
    history.setOperatorId(operatorId);
    history.setEntityName(entityName);
    history.setFieldName(fieldName);
    history.setOldValue(oldValue);
    history.setNewValue(newValue);

    historyManagementRepository.save(history);
  }

  public List<String> totalRank() {

    List<Officer> officers = officerRepository.findAll();

    // extract unique ranks
    List<String> uniqueRanks = officers.stream()
        .map(Officer::getRank) // get rank field
        .distinct() // keep only unique ranks
        .toList(); // convert to list

    return uniqueRanks;
  }
}
