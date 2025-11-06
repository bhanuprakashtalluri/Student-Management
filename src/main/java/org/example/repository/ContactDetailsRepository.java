package org.example.repository;

import org.example.entity.ContactDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactDetailsRepository extends JpaRepository<ContactDetails, Long> {
    List<ContactDetails> findByStudentStudentNumber(Long studentNumber);
}
