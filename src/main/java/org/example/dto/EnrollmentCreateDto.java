package org.example.dto;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EnrollmentCreateDto {
    @NotNull private Long courseNumber; // reference existing course
    @NotNull private LocalDate enrollmentDate;
    @NotNull private Integer overallGrade;
    @NotBlank private String semester;
    @NotBlank private String instructorName;
}

