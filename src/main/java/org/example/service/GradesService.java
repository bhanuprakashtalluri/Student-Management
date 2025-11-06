package org.example.service;

import org.example.entity.Grades;
import java.util.List;

public interface GradesService {
    Grades createGrade(Grades grade);
    Grades getGradeById(Long id);
    List<Grades> getAllGrades();
    Grades updateGrade(Long id, Grades updated);
    void deleteGrade(Long id);
}
