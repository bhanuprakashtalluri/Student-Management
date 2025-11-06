package org.example.repository;

import org.example.entity.AttendanceData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceDataRepository extends JpaRepository<AttendanceData, Long> {
    List<AttendanceData> findByStudentStudentNumber(Long studentNumber);
}
