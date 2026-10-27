package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.ContactLead;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactLeadRepository extends JpaRepository<ContactLead, Long> {
    long countByIsReadFalse();
    List<ContactLead> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
