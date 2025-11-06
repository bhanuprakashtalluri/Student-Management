package org.example.dto;
import lombok.*;
import jakarta.validation.constraints.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AddressCreateDto {
    @NotBlank private String street;
    @NotBlank private String city;
    @NotBlank private String state;
    @NotBlank private String zipCode;
}

