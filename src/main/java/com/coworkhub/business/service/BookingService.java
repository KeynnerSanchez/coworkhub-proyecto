package com.coworkhub.business.service;

import com.coworkhub.dto.BookingItemRequest;
import com.coworkhub.dto.BookingRequest;
import com.coworkhub.dto.BookingResponse;
import com.coworkhub.persistence.enums.BookingStatus;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    BookingResponse create(BookingRequest request);
    BookingResponse findById(Long id);
    List<BookingResponse> findByMember(Long miembroId);
    List<BookingResponse> findByRoomAndDate(Long salaId, LocalDate fecha);
    List<BookingResponse> findByStatus(BookingStatus estado);
    BookingResponse addService(Long reservaId, BookingItemRequest request);
    BookingResponse removeService(Long reservaId, Long servicioId);
    BookingResponse confirm(Long id);
    BookingResponse cancel(Long id);
    BookingResponse complete(Long id);
}
