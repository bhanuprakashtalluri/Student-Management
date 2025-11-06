package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.entity.EnrollmentData;
import org.example.repository.EnrollmentDataRepository;
import org.example.service.EnrollmentDataService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentDataServiceImpl implements EnrollmentDataService {
    private final EnrollmentDataRepository repository;

    @Override
    public EnrollmentData createEnrollment(EnrollmentData enrollment) { return repository.save(enrollment); }

    @Override
    public EnrollmentData getEnrollmentByNumber(Long enrollmentNumber) { return repository.findById(enrollmentNumber).orElse(null); }

    @Override
    public List<EnrollmentData> getAllEnrollments() { return repository.findAll(); }

    @Override
    public EnrollmentData updateEnrollment(Long enrollmentNumber, EnrollmentData updated) {
        EnrollmentData existing = repository.findById(enrollmentNumber).orElse(null);
        if(existing==null) return null;
        updated.setEnrollmentNumber(existing.getEnrollmentNumber());
        return repository.save(updated);
    }

    @Override
    public void deleteEnrollmentByNumber(Long enrollmentNumber) { repository.deleteById(enrollmentNumber); }
}
