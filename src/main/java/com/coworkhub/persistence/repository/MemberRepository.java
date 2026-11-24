package com.coworkhub.persistence.repository;

import com.coworkhub.persistence.entity.Member;
import com.coworkhub.persistence.enums.MemberStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Override
    @EntityGraph(attributePaths = {"plan", "account"})
    List<Member> findAll();

    @Override
    @EntityGraph(attributePaths = {"plan", "account"})
    Optional<Member> findById(Long id);

    @EntityGraph(attributePaths = {"plan", "account"})
    List<Member> findByStatus(MemberStatus status);

    @EntityGraph(attributePaths = {"plan", "account"})
    List<Member> findByFullNameContainingIgnoreCase(String fullName);

    @EntityGraph(attributePaths = {"plan", "account"})
    List<Member> findByFullNameContainingIgnoreCaseAndStatus(String fullName, MemberStatus status);

    Optional<Member> findByAccountId(Long accountId);

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumberAndIdNot(String documentNumber, Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByPlanId(Long planId);
}
