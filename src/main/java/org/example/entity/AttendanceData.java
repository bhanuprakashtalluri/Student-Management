package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendance_data", indexes = {
        @Index(name = "idx_attendance_enrollment", columnList = "enrollment_number"),
        @Index(name = "idx_attendance_student", columnList = "student_number"),
        @Index(name = "idx_attendance_date", columnList = "attendance_date"),
        @Index(name = "idx_attendance_status", columnList = "attendance_status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_number")
    private Long attendanceNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "enrollment_number", referencedColumnName = "enrollment_number", nullable = false)
    private EnrollmentData enrollment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_number", referencedColumnName = "student_number", nullable = false)
    private StudentData student;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false, length = 10)
    private AttendanceStatus attendanceStatus = AttendanceStatus.PRESENT;

    @Column(name = "semester", length = 20)
    private String semester;

    public enum AttendanceStatus { PRESENT, ABSENT, EXCUSED }
}
