package org.example.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentDataDto {
    @Pattern(regexp = "\\d+", message = "studentNumber must contain only digits")
    private String studentNumber;
    @NotNull @Size(max = 100)
    private String firstName;
    @NotNull @Size(max = 100)
    private String lastName;
    @NotNull
    private LocalDate dateOfBirth;
    @NotNull
    private Integer gender;
    @NotNull
    private LocalDate joiningDate;
    @NotNull
    private Integer studentStatus;
}
