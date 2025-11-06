package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_details", indexes = {
        @Index(name = "idx_course_name", columnList = "course_name"),
        @Index(name = "idx_course_code", columnList = "course_code"),
        @Index(name = "idx_course_credits", columnList = "course_credits")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_course_name", columnNames = {"course_name"}),
        @UniqueConstraint(name = "uk_course_code", columnNames = {"course_code"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_number")
    private Long courseNumber;

    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    @Column(name = "course_code", nullable = false, length = 10)
    private String courseCode;

    @Column(name = "course_credits", nullable = false)
    private Double courseCredits;
}
