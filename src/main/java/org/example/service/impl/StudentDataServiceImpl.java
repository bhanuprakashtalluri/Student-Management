package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dto.AddressCreateDto;
import org.example.dto.ContactCreateDto;
import org.example.dto.EnrollmentCreateDto;
import org.example.dto.StudentAggregateCreateRequest;
import org.example.entity.AddressDetails;
import org.example.entity.ContactDetails;
import org.example.entity.EnrollmentData;
import org.example.entity.CourseDetails;
import org.example.entity.StudentData;
import org.example.repository.*;
import org.example.service.StudentDataService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentDataServiceImpl implements StudentDataService {
    private final StudentDataRepository repository;
    private final AddressDetailsRepository addressRepository;
    private final ContactDetailsRepository contactRepository;
    private final EnrollmentDataRepository enrollmentRepository;
    private final AttendanceDataRepository attendanceRepository;
    private final CourseDetailsRepository courseDetailsRepository;

    @Override
    public StudentData createStudent(StudentData student) {
        return repository.save(student);
    }

    @Override
    public StudentData getStudentByNumber(Long studentNumber) {
        return repository.findByStudentNumber(studentNumber).orElse(null);
    }

    @Override
    public List<StudentData> getAllStudents() {
        return repository.findAll();
    }

    @Override
    public StudentData updateStudent(Long studentNumber, StudentData updated) {
        StudentData existing = repository.findByStudentNumber(studentNumber).orElse(null);
        if(existing==null) return null;
        updated.setStudentNumber(existing.getStudentNumber());
        return repository.save(updated);
    }

    @Override
    public void deleteStudent(Long studentNumber) {
        StudentData student = repository.findByStudentNumber(studentNumber).orElse(null);
        if(student==null) return;
        // Delete enrollments (cascade removes grades & attendance)
        enrollmentRepository.deleteAll(enrollmentRepository.findByStudentStudentNumber(studentNumber));
        contactRepository.deleteAll(contactRepository.findByStudentStudentNumber(studentNumber));
        addressRepository.deleteAll(addressRepository.findByStudentStudentNumber(studentNumber));
        repository.deleteById(studentNumber);
    }

    @Override
    public StudentData createStudentAggregate(StudentAggregateCreateRequest request) {
        StudentData student = StudentData.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(StudentData.Gender.values()[request.getGender()])
                .joiningDate(request.getJoiningDate())
                .studentStatus(StudentData.StudentStatus.values()[request.getStudentStatus()])
                .build();
        student = repository.save(student);
        // persist addresses
        for(AddressCreateDto a : request.getAddresses()) {
            addressRepository.save(AddressDetails.builder()
                    .student(student)
                    .street(a.getStreet())
                    .city(a.getCity())
                    .state(a.getState())
                    .zipCode(a.getZipCode())
                    .build());
        }
        for(ContactCreateDto c : request.getContacts()) {
            contactRepository.save(ContactDetails.builder()
                    .student(student)
                    .emailAddress(c.getEmailAddress())
                    .mobileNumber(c.getMobileNumber())
                    .build());
        }
        for(EnrollmentCreateDto e : request.getEnrollments()) {
            CourseDetails course = courseDetailsRepository.findByCourseNumber(e.getCourseNumber()).orElse(null);
            if(course == null) continue; // skip invalid
            enrollmentRepository.save(EnrollmentData.builder()
                    .student(student)
                    .course(course)
                    .enrollmentDate(e.getEnrollmentDate())
                    .overallGrade(e.getOverallGrade())
                    .semester(e.getSemester())
                    .instructorName(e.getInstructorName())
                    .build());
        }
        return student;
    }
}
