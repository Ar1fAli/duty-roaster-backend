package com.infotech.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.infotech.entity.HistoryManagement;
import com.infotech.entity.UserEntity;
import com.infotech.repository.HistoryManagementRepository;
import com.infotech.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final HistoryManagementRepository historyManagementRepository;
    private final PasswordEncoder encoder;

    public UserEntity updateUser(Long id, UserEntity updatedUser, String operatedBy) {
        return userRepository.findById(id).map(user -> {

            // OPTIONAL: uniqueness validation (email/username/etc)
            // validateUniqueFields(updatedUser, id);

            List<HistoryManagement> historyEntries = new ArrayList<>();

            // helper: log change for any field
            BiConsumer<String, Object[]> logChange = (fieldName, values) -> {
                Object oldVal = values[0];
                Object newVal = values[1];

                // if same or both null → no history
                if (Objects.equals(oldVal, newVal)) {
                    return;
                }

                HistoryManagement history = new HistoryManagement();
                history.setTime(LocalDateTime.now());
                history.setOperationType("UPDATE");
                history.setOperatedBy(operatedBy);
                history.setOperatorId(id); // user id
                history.setEntityName("Manager"); // or "VIP_USER" etc.
                history.setFieldName(fieldName);
                history.setOldValue(oldVal == null ? null : oldVal.toString());
                history.setNewValue(newVal == null ? null : newVal.toString());

                historyEntries.add(history);
            };

            // ==== NULL-SAFE FIELD UPDATES + HISTORY ====

            // name
            if (updatedUser.getName() != null && !updatedUser.getName().isBlank()) {
                logChange.accept("name", new Object[] {
                        user.getName(),
                        updatedUser.getName()
                });
                user.setName(updatedUser.getName());
            }

            // email
            if (updatedUser.getEmail() != null && !updatedUser.getEmail().isBlank()) {
                logChange.accept("email", new Object[] {
                        user.getEmail(),
                        updatedUser.getEmail()
                });
                user.setEmail(updatedUser.getEmail());
            }

            // status
            if (updatedUser.getStatus() != null && !updatedUser.getStatus().isBlank()) {
                logChange.accept("status", new Object[] {
                        user.getStatus(),
                        updatedUser.getStatus()
                });
                user.setStatus(updatedUser.getStatus());
            }

            // contactno
            if (updatedUser.getContactno() != null && !updatedUser.getContactno().isBlank()) {
                logChange.accept("contactno", new Object[] {
                        user.getContactno(),
                        updatedUser.getContactno()
                });
                user.setContactno(updatedUser.getContactno());
            }

            // username
            if (updatedUser.getUsername() != null && !updatedUser.getUsername().isBlank()) {
                logChange.accept("username", new Object[] {
                        user.getUsername(),
                        updatedUser.getUsername()
                });
                user.setUsername(updatedUser.getUsername());
            }

            // ==== PASSWORD (special handling) ====
            String newRawPassword = updatedUser.getPassword();

            if (newRawPassword != null && !newRawPassword.isBlank()) {

                boolean sameAsOld = encoder.matches(newRawPassword, user.getPassword());

                if (!sameAsOld) {
                    // Do NOT store old/new password in plain text
                    HistoryManagement history = new HistoryManagement();
                    history.setTime(LocalDateTime.now());
                    history.setOperationType("UPDATE");
                    history.setOperatedBy(operatedBy);
                    history.setOperatorId(id);
                    history.setEntityName("Manager");
                    history.setFieldName("password");
                    history.setOldValue(null); // or "********"
                    history.setNewValue("UPDATED"); // just mark that it changed

                    historyEntries.add(history);

                    user.setPassword(encoder.encode(newRawPassword));
                }
            }

            // ==== SAVE USER + HISTORY ====
            UserEntity saved = userRepository.save(user);

            if (!historyEntries.isEmpty()) {
                historyManagementRepository.saveAll(historyEntries);
            }

            return saved;

        }).orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }
}
