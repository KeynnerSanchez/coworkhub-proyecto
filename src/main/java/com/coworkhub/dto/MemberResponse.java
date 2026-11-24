package com.coworkhub.dto;

import com.coworkhub.persistence.enums.MemberStatus;

public record MemberResponse(Long id, String documentNumber, String fullName, String email,
                             MemberStatus status, Long planId, String planName, String username) {
}
