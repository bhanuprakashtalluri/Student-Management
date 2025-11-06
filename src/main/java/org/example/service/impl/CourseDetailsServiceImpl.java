package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.entity.CourseDetails;
import org.example.repository.CourseDetailsRepository;
import org.example.service.CourseDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseDetailsServiceImpl implements CourseDetailsService {
    private final CourseDetailsRepository repository;

    @Override
    public CourseDetails createCourse(CourseDetails course) { return repository.save(course); }

    @Override
    public CourseDetails getCourseByNumber(Long courseNumber) { return repository.findByCourseNumber(courseNumber).orElse(null); }

    @Override
    public List<CourseDetails> getAllCourses() { return repository.findAll(); }

    @Override
    public CourseDetails updateCourse(Long courseNumber, CourseDetails updated) {
        CourseDetails existing = repository.findByCourseNumber(courseNumber).orElse(null);
        if(existing==null) return null;
        updated.setCourseNumber(existing.getCourseNumber());
        return repository.save(updated);
    }

    @Override
    public void deleteCourse(Long courseNumber) { repository.deleteById(courseNumber); }
}
