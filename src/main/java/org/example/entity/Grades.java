package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "grades", indexes = {
        @Index(name = "idx_grade_enrollment", columnList = "enrollment_number"),
        @Index(name = "idx_grade_assessment_date", columnList = "assessment_date"),
        @Index(name = "idx_grade_assessment_type", columnList = "assessment_type")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Grades {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_number")
    private Long gradeNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "enrollment_number", referencedColumnName = "enrollment_number", nullable = false)
    private EnrollmentData enrollment;

    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate;

    @Column(name = "assessment_type", nullable = false, length = 50)
    private String assessmentType;

    @Column(name = "obtained_score", nullable = false)
    private Integer obtainedScore;

    @Builder.Default
    @Column(name = "max_score", nullable = false)
    private Integer maxScore = 100;

    @Column(name = "grade_code", nullable = false)
    private Integer gradeCode; // 0=A,1=B,2=C,3=D,4=F,-1=unknown
}
