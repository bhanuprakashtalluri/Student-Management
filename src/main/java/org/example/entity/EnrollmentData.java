package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "enrollment_data", indexes = {
        @Index(name = "idx_enrollment_student", columnList = "student_number"),
        @Index(name = "idx_enrollment_course", columnList = "course_number"),
        @Index(name = "idx_enrollment_date", columnList = "enrollment_date"),
        @Index(name = "idx_enrollment_overall_grade", columnList = "overall_grade"),
        @Index(name = "idx_enrollment_semester", columnList = "semester"),
        @Index(name = "idx_enrollment_instructor", columnList = "instructor_name")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_course_semester", columnNames = {"student_number", "course_number", "semester"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EnrollmentData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_number")
    private Long enrollmentNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_number", referencedColumnName = "student_number", nullable = false)
    private StudentData student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_number", referencedColumnName = "course_number", nullable = false)
    private CourseDetails course;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Column(name = "overall_grade", nullable = false)
    private Integer overallGrade;

    @Column(name = "semester", nullable = false, length = 20)
    private String semester;

    @Column(name = "instructor_name", nullable = false, length = 100)
    private String instructorName;

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<Grades> grades = new ArrayList<>();

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<AttendanceData> attendanceRecords = new ArrayList<>();
}
