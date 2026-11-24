package com.coworkhub.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_items")
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "additional_service_id")
    private AdditionalService service;

    @Column(nullable = false)
    private int quantity;

    // Precio que tenia el servicio cuando se agrego (si el precio cambia despues, esto no cambia)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal appliedUnitPrice;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public AdditionalService getService() { return service; }
    public void setService(AdditionalService service) { this.service = service; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getAppliedUnitPrice() { return appliedUnitPrice; }
    public void setAppliedUnitPrice(BigDecimal appliedUnitPrice) { this.appliedUnitPrice = appliedUnitPrice; }
}
