package org.example.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseDetailsDto {
    private String courseNumber; // auto-incremented integer as string for DTO
    @NotNull @Size(max = 100)
    private String courseName;
    @NotNull @Size(max = 10)
    private String courseCode;
    @NotNull
    private Double courseCredits;
}
