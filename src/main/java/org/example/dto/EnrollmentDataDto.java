package org.example.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EnrollmentDataDto {
    private String enrollmentNumber; // auto-incremented integer as string for DTO
    @NotNull
    private String studentNumber; // FK reference to StudentData
    @NotNull
    private String courseNumber; // FK reference to CourseDetails
    @NotNull
    private LocalDate enrollmentDate;
    @NotNull
    private Integer overallGrade;
    @NotNull @Size(max = 20)
    private String semester;
    @NotNull @Size(max = 100)
    private String instructorName;
}
