package org.example.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class GenderConverter implements AttributeConverter<StudentData.Gender, Integer> {
    @Override
    public Integer convertToDatabaseColumn(StudentData.Gender attribute) {
        if(attribute == null) return null;
        return switch (attribute) {
            case MALE -> 0; case FEMALE -> 1; case OTHER -> 2; };
    }
    @Override
    public StudentData.Gender convertToEntityAttribute(Integer dbData) {
        if(dbData == null) return StudentData.Gender.OTHER;
        return switch (dbData) {
            case 0 -> StudentData.Gender.MALE;
            case 1 -> StudentData.Gender.FEMALE;
            case 2 -> StudentData.Gender.OTHER;
            default -> StudentData.Gender.OTHER;
        };
    }
}

