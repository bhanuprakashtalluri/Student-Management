package org.example.service;

import org.example.entity.*;
import org.example.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:testdb3;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class StudentCascadeDeleteTest {

    @Autowired private StudentDataService studentService;
    @Autowired private AddressDetailsRepository addressRepo;
    @Autowired private ContactDetailsRepository contactRepo;
    @Autowired private EnrollmentDataRepository enrollmentRepo;
    @Autowired private AttendanceDataRepository attendanceRepo;
    @Autowired private CourseDetailsRepository courseRepo;

    @Test
    void deletingStudentRemovesDependents() {
        // Create student
        StudentData student = StudentData.builder()
                .firstName("John")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(2001,2,3))
                .gender(StudentData.Gender.MALE)
                .joiningDate(LocalDate.of(2024,9,1))
                .studentStatus(StudentData.StudentStatus.ACTIVE)
                .build();
        student = studentService.createStudent(student);
        Long studentNumber = student.getStudentNumber();
        assertNotNull(studentNumber);

        // Create course for enrollment
        CourseDetails course = CourseDetails.builder()
                .courseName("Mathematics")
                .courseCode("MATH101")
                .courseCredits(3.0)
                .build();
        course = courseRepo.save(course);

        // Add address
        addressRepo.save(AddressDetails.builder()
                .student(student)
                .street("123 Main St")
                .city("Metropolis")
                .state("CA")
                .zipCode("90210")
                .build());

        // Add contact
        contactRepo.save(ContactDetails.builder()
                .student(student)
                .emailAddress("john.smith@example.com")
                .mobileNumber("1234567890")
                .build());

        // Add enrollment
        EnrollmentData enrollment = EnrollmentData.builder()
                .student(student)
                .course(course)
                .enrollmentDate(LocalDate.of(2024,9,10))
                .overallGrade(90)
                .semester("FALL24")
                .instructorName("Prof. Euler")
                .build();
        enrollment = enrollmentRepo.save(enrollment);

        // Add attendance
        attendanceRepo.save(AttendanceData.builder()
                .student(student)
                .enrollment(enrollment)
                .attendanceDate(LocalDate.of(2024,9,11))
                .attendanceStatus(AttendanceData.AttendanceStatus.PRESENT)
                .semester("FALL24")
                .build());

        assertFalse(addressRepo.findByStudentStudentNumber(studentNumber).isEmpty());
        assertFalse(contactRepo.findByStudentStudentNumber(studentNumber).isEmpty());
        assertFalse(enrollmentRepo.findByStudentStudentNumber(studentNumber).isEmpty());
        assertFalse(attendanceRepo.findByStudentStudentNumber(studentNumber).isEmpty());

        // Delete student
        studentService.deleteStudent(studentNumber);

        assertTrue(addressRepo.findByStudentStudentNumber(studentNumber).isEmpty(), "Addresses should be deleted");
        assertTrue(contactRepo.findByStudentStudentNumber(studentNumber).isEmpty(), "Contacts should be deleted");
        assertTrue(enrollmentRepo.findByStudentStudentNumber(studentNumber).isEmpty(), "Enrollments should be deleted");
        assertTrue(attendanceRepo.findByStudentStudentNumber(studentNumber).isEmpty(), "Attendance should be deleted");
    }
}
