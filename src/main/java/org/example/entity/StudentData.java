package org.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "student_data", indexes = {
        @Index(name = "idx_student_last_name", columnList = "last_name"),
        @Index(name = "idx_student_first_name", columnList = "first_name"),
        @Index(name = "idx_student_joining_date", columnList = "joining_date"),
        @Index(name = "idx_student_status", columnList = "student_status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_number")
    private Long studentNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "student_status", nullable = false, length = 20)
    private StudentStatus studentStatus = StudentStatus.ACTIVE;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<AddressDetails> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<ContactDetails> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<EnrollmentData> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<AttendanceData> attendanceRecords = new ArrayList<>();

    public enum Gender { MALE, FEMALE, OTHER }
    public enum StudentStatus { ACTIVE, INACTIVE, GRADUATED }
}
