package org.example.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErrorDto {
    private int status;
    private String message;
    private String details;
}
