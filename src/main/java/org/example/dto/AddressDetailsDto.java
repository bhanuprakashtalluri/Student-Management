package org.example.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AddressDetailsDto {
    private Long addressNumber;
    @NotNull
    private String studentNumber; // FK reference to StudentData
    @NotNull @Size(max = 200)
    private String street;
    @NotNull @Size(max = 100)
    private String city;
    @NotNull @Size(max = 50)
    private String state;
    @NotNull @Size(max = 20)
    private String zipCode;
}
