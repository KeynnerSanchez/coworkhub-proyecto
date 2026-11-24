package com.coworkhub.persistence.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "membership_plans")
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int includedHoursPerMonth;

    // Porcentaje de descuento (0 a 100) sobre las horas excedentes
    @Column(nullable = false)
    private int overageDiscount;

    @Column(nullable = false)
    private int maxActiveBookings;

    // Un plan tiene muchos miembros (lado inverso)
    @OneToMany(mappedBy = "plan")
    private List<Member> members = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getIncludedHoursPerMonth() { return includedHoursPerMonth; }
    public void setIncludedHoursPerMonth(int includedHoursPerMonth) { this.includedHoursPerMonth = includedHoursPerMonth; }
    public int getOverageDiscount() { return overageDiscount; }
    public void setOverageDiscount(int overageDiscount) { this.overageDiscount = overageDiscount; }
    public int getMaxActiveBookings() { return maxActiveBookings; }
    public void setMaxActiveBookings(int maxActiveBookings) { this.maxActiveBookings = maxActiveBookings; }
    public List<Member> getMembers() { return members; }
    public void setMembers(List<Member> members) { this.members = members; }
}
