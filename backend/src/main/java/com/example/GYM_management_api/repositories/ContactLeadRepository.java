package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.ContactLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactLeadRepository extends JpaRepository<ContactLead, Long> {
}
