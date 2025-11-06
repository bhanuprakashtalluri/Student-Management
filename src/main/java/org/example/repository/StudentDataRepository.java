package org.example.repository;

import org.example.entity.StudentData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentDataRepository extends JpaRepository<StudentData, Long> { // changed ID type to Long
    Optional<StudentData> findByStudentNumber(Long studentNumber);
}
