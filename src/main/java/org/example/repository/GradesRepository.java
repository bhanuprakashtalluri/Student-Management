package org.example.repository;

import org.example.entity.Grades;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GradesRepository extends JpaRepository<Grades, Long> {
    List<Grades> findByEnrollmentEnrollmentNumber(Long enrollmentNumber);
}
