package org.example.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceDataDto {
    private Long attendanceNumber;
    @NotNull
    private Long enrollmentNumber;
    @NotNull
    private String studentNumber; // FK reference to StudentData
    @NotNull
    private LocalDate attendanceDate;
    @NotNull
    private Integer attendanceStatus; // ordinal
    private String semester; // optional
}
