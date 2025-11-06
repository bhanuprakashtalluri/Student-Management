package org.example.service;

import org.example.entity.EnrollmentData;
import java.util.List;

public interface EnrollmentDataService {
    EnrollmentData createEnrollment(EnrollmentData enrollment);
    EnrollmentData getEnrollmentByNumber(Long enrollmentNumber);
    List<EnrollmentData> getAllEnrollments();
    EnrollmentData updateEnrollment(Long enrollmentNumber, EnrollmentData updated);
    void deleteEnrollmentByNumber(Long enrollmentNumber);
}
