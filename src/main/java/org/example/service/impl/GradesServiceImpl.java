package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.entity.Grades;
import org.example.repository.GradesRepository;
import org.example.service.GradesService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GradesServiceImpl implements GradesService {
    private final GradesRepository repository;

    @Override
    public Grades createGrade(Grades grade) { return repository.save(grade); }

    @Override
    public Grades getGradeById(Long id) { return repository.findById(id).orElse(null); }

    @Override
    public List<Grades> getAllGrades() { return repository.findAll(); }

    @Override
    public Grades updateGrade(Long id, Grades updated) {
        Optional<Grades> existing = repository.findById(id);
        if (existing.isEmpty()) return null;
        updated.setGradeNumber(id);
        return repository.save(updated);
    }

    @Override
    public void deleteGrade(Long id) { repository.deleteById(id); }
}
