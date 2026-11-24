package com.coworkhub.business.mapper;

import com.coworkhub.dto.*;
import com.coworkhub.persistence.entity.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

// Convierte entidades en DTO. Las entidades nunca salen por la API
public final class Mapeador {

    private Mapeador() {
    }

    public static LocationResponse toResponse(Location l) {
        return new LocationResponse(l.getId(), l.getName(), l.getCity(), l.getAddress(),
                l.getOpeningTime(), l.getClosingTime());
    }

    public static EquipmentResponse toResponse(Equipment e) {
        return new EquipmentResponse(e.getId(), e.getName());
    }

    public static AdditionalServiceResponse toResponse(AdditionalService s) {
        return new AdditionalServiceResponse(s.getId(), s.getName(), s.getUnitPrice());
    }

    public static MembershipPlanResponse toResponse(MembershipPlan p) {
        return new MembershipPlanResponse(p.getId(), p.getName(), p.getIncludedHoursPerMonth(),
                p.getOverageDiscount(), p.getMaxActiveBookings());
    }

    public static RoomResponse toResponse(Room r) {
        List<EquipmentResponse> equipos = r.getEquipment().stream()
                .sorted(Comparator.comparing(Equipment::getId))
                .map(Mapeador::toResponse)
                .toList();
        return new RoomResponse(r.getId(), r.getName(), r.getType(), r.getCapacity(), r.getHourlyRate(),
                r.isActive(), r.getLocation().getId(), r.getLocation().getName(), equipos);
    }

    public static MemberResponse toResponse(Member m) {
        String username = m.getAccount() == null ? null : m.getAccount().getUsername();
        return new MemberResponse(m.getId(), m.getDocumentNumber(), m.getFullName(), m.getEmail(),
                m.getStatus(), m.getPlan().getId(), m.getPlan().getName(), username);
    }

    public static BookingResponse toResponse(Booking b) {
        List<BookingItemResponse> lineas = b.getItems().stream()
                .map(i -> new BookingItemResponse(i.getService().getId(), i.getService().getName(), i.getQuantity(),
                        i.getAppliedUnitPrice(), i.getAppliedUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
                .toList();
        return new BookingResponse(b.getId(), b.getMember().getId(), b.getMember().getFullName(),
                b.getRoom().getId(), b.getRoom().getName(), b.getStartTime(), b.getEndTime(), b.getAttendees(),
                b.getStatus(), b.getRoomCost(), b.getServicesCost(), b.getTotalCost(), b.getCancellationFee(), lineas);
    }
}
