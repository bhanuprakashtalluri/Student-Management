package org.example.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentAggregateCreateRequest {
    @NotNull @Size(max=100) private String firstName;
    @NotNull @Size(max=100) private String lastName;
    @NotNull private LocalDate dateOfBirth;
    @NotNull private Integer gender; // ordinal
    @NotNull private LocalDate joiningDate;
    @NotNull private Integer studentStatus; // ordinal

    @Builder.Default private List<AddressCreateDto> addresses = List.of();
    @Builder.Default private List<ContactCreateDto> contacts = List.of();
    @Builder.Default private List<EnrollmentCreateDto> enrollments = List.of();
}

