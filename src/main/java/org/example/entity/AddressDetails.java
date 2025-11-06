package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "address_details", indexes = {
        @Index(name = "idx_address_student", columnList = "student_number"),
        @Index(name = "idx_address_city", columnList = "city"),
        @Index(name = "idx_address_state", columnList = "state"),
        @Index(name = "idx_address_zip", columnList = "zip_code")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AddressDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_number")
    private Long addressNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_number", referencedColumnName = "student_number", nullable = false)
    private StudentData student;

    @Column(name = "street", nullable = false, length = 200)
    private String street;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 50)
    private String state;

    @Column(name = "zip_code", nullable = false, length = 20)
    private String zipCode;
}
