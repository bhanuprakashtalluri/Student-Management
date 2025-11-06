package org.example.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GradesDto {
    private Long gradeNumber;
    @NotNull
    private Long enrollmentNumber;
    @NotNull
    private LocalDate assessmentDate;
    @NotNull @Size(max = 50)
    private String assessmentType;
    @NotNull @Min(0) @Max(100)
    private Integer obtainedScore;
    @NotNull @Min(1) @Max(100)
    @Builder.Default
    private Integer maxScore = 100;
    private Integer gradeCode; // 0=A etc.
}
