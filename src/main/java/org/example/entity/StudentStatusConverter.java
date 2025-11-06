package org.example.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class StudentStatusConverter implements AttributeConverter<StudentData.StudentStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(StudentData.StudentStatus attribute) {
        if(attribute == null) return null;
        return switch (attribute) {
            case ACTIVE -> 0; case INACTIVE -> 1; case GRADUATED -> 2; };
    }
    @Override
    public StudentData.StudentStatus convertToEntityAttribute(Integer dbData) {
        if(dbData == null) return StudentData.StudentStatus.ACTIVE;
        return switch (dbData) {
            case 0 -> StudentData.StudentStatus.ACTIVE;
            case 1 -> StudentData.StudentStatus.INACTIVE;
            case 2 -> StudentData.StudentStatus.GRADUATED;
            default -> StudentData.StudentStatus.ACTIVE;
        };
    }
}

