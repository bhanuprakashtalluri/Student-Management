package org.example.repository;

import org.example.entity.CourseDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseDetailsRepository extends JpaRepository<CourseDetails, Long> { // changed ID type to Long
    Optional<CourseDetails> findByCourseNumber(Long courseNumber);
}
