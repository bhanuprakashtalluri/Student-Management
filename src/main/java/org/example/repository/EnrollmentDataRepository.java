package org.example.repository;

import org.example.entity.EnrollmentData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentDataRepository extends JpaRepository<EnrollmentData, Long> {
    List<EnrollmentData> findByStudentStudentNumber(Long studentNumber);
}
