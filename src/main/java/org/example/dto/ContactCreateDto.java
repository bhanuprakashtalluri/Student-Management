package org.example.dto;
import lombok.*;
import jakarta.validation.constraints.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContactCreateDto {
    @Email @NotBlank private String emailAddress;
    @NotBlank private String mobileNumber;
}

