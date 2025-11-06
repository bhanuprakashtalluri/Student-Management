package org.example.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContactDetailsDto {
    private Long contactNumber;
    @NotNull
    private String studentNumber;
    @NotNull @Size(max = 100) @Email
    private String emailAddress;
    @NotNull @Size(max = 15)
    private String mobileNumber;
}
