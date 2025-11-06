package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contact_details", indexes = {
        @Index(name = "idx_contact_student", columnList = "student_number"),
        @Index(name = "idx_contact_email", columnList = "email_address"),
        @Index(name = "idx_contact_mobile", columnList = "mobile_number")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_contact_email", columnNames = {"email_address"}),
        @UniqueConstraint(name = "uk_contact_mobile", columnNames = {"mobile_number"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContactDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_number")
    private Long contactNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_number", referencedColumnName = "student_number", nullable = false)
    private StudentData student;

    @Column(name = "email_address", nullable = false, length = 100)
    private String emailAddress;

    @Column(name = "mobile_number", nullable = false, length = 15)
    private String mobileNumber;
}
