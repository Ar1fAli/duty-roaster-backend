package com.infotech.controller;

import java.util.List;

import com.infotech.entity.AssignmentValue;
import com.infotech.entity.SecurityType;
import com.infotech.repository.AssignmentValueRepository;
import com.infotech.repository.SecurityTypeRepository;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/setting")
public class SettingController {
    private final SecurityTypeRepository securityTypeRepository;
    private final AssignmentValueRepository assignmentValueRepository;

    @PostMapping("/set")
    public Long setSecurityType(@RequestBody SecurityType security) {
        SecurityType sec = new SecurityType();
        sec.setName(security.getName());
        System.out.println(security + " security is this value ");
        List<AssignmentValue> values = security.getValues().stream()
                .map(d -> AssignmentValue.builder()
                        .rank(d.getRank())
                        .value(d.getValue())
                        .type(sec) // or savedSec if you saved it above
                        .build())
                .toList();

        sec.setValues(values); // or ensure list initialized and use addAll

        SecurityType saved = securityTypeRepository.save(sec);
        return saved.getId();
    }

}
