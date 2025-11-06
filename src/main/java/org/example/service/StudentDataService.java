package org.example.service;

import org.example.entity.StudentData;
import java.util.List;

public interface StudentDataService {
    StudentData createStudent(StudentData student);
    StudentData getStudentByNumber(Long studentNumber);
    List<StudentData> getAllStudents();
    StudentData updateStudent(Long studentNumber, StudentData updated);
    void deleteStudent(Long studentNumber);
    StudentData createStudentAggregate(org.example.dto.StudentAggregateCreateRequest request);
}
