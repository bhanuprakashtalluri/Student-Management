package org.example.repository;

import org.example.entity.AddressDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressDetailsRepository extends JpaRepository<AddressDetails, Long> {
    List<AddressDetails> findByStudentStudentNumber(Long studentNumber);
}
