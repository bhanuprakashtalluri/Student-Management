package org.example.service;

import org.example.entity.CourseDetails;
import java.util.List;

public interface CourseDetailsService {
    CourseDetails createCourse(CourseDetails course);
    CourseDetails getCourseByNumber(Long courseNumber);
    List<CourseDetails> getAllCourses();
    CourseDetails updateCourse(Long courseNumber, CourseDetails updated);
    void deleteCourse(Long courseNumber);
}
