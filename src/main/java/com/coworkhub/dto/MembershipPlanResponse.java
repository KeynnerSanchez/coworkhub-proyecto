package com.coworkhub.dto;

public record MembershipPlanResponse(Long id, String name, int includedHoursPerMonth,
                                     int overageDiscount, int maxActiveBookings) {
}
