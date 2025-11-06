package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.entity.EnrollmentData;
import org.example.entity.StudentData;
import org.example.entity.CourseDetails;
import org.example.service.EnrollmentDataService;
import org.example.service.StudentDataService;
import org.example.service.CourseDetailsService;
import org.example.dto.EnrollmentDataDto;
import org.example.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentDataService enrollmentService;
    private final StudentDataService studentService;
    private final CourseDetailsService courseService;

    @GetMapping
    public List<EnrollmentData> list(){ return enrollmentService.getAllEnrollments(); }

    @PostMapping("/upload-csv")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if(file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Empty file"));
        List<EnrollmentData> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))){
            String headerLine = reader.readLine();
            if(headerLine == null) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Missing header row"));
            String[] headersRaw = headerLine.split(",");
            Map<String,Integer> idx = new HashMap<>();
            for(int i=0;i<headersRaw.length;i++) idx.put(headersRaw[i].trim().toLowerCase(), i);
            String[] required = {"studentnumber","coursenumber","enrollmentdate","overallgrade","semester","instructorname"};
            for(String r: required) if(!idx.containsKey(r)) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Missing column: "+r));
            String line; int row=1;
            while((line=reader.readLine())!=null){
                row++; if(line.isBlank()) continue; String[] parts = line.split(",", -1);
                try {
                    Long studentNumber = Long.parseLong(get(parts, idx.get("studentnumber")));
                    Long courseNumber = Long.parseLong(get(parts, idx.get("coursenumber")));
                    StudentData student = studentService.getStudentByNumber(studentNumber);
                    CourseDetails course = courseService.getCourseByNumber(courseNumber);
                    if(student == null || course == null) throw new IllegalArgumentException("Missing FK student or course");
                    EnrollmentData ed = EnrollmentData.builder()
                            .student(student)
                            .course(course)
                            .enrollmentDate(LocalDate.parse(get(parts, idx.get("enrollmentdate"))))
                            .overallGrade(Integer.parseInt(get(parts, idx.get("overallgrade"))))
                            .semester(get(parts, idx.get("semester")))
                            .instructorName(get(parts, idx.get("instructorname")))
                            .build();
                    created.add(enrollmentService.createEnrollment(ed));
                } catch(Exception ex){ errors.add("Row "+row+": "+ex.getMessage()); }
            }
        }
        if(!errors.isEmpty()) return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(Map.of("inserted",created.size(),"errors",errors,"enrollments",created));
        return ResponseEntity.ok(created);
    }

    private String get(String[] parts, int i){ return (i>=0 && i<parts.length)? parts[i].trim():""; }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody EnrollmentDataDto dto){
        try {
            StudentData student = studentService.getStudentByNumber(Long.parseLong(dto.getStudentNumber()));
            CourseDetails course = courseService.getCourseByNumber(Long.parseLong(dto.getCourseNumber()));
            if(student==null || course==null) return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid FK student or course").build());
            EnrollmentData entity = EnrollmentData.builder()
                    .student(student)
                    .course(course)
                    .enrollmentDate(dto.getEnrollmentDate())
                    .overallGrade(dto.getOverallGrade())
                    .semester(dto.getSemester())
                    .instructorName(dto.getInstructorName())
                    .build();
            EnrollmentData saved = enrollmentService.createEnrollment(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapEntity(saved));
        } catch(Exception ex){
            return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid enrollment data").details(ex.getMessage()).build());
        }
    }

    @GetMapping("/{enrollmentNumber}")
    public ResponseEntity<?> get(@PathVariable Long enrollmentNumber){
        EnrollmentData ed = enrollmentService.getEnrollmentByNumber(enrollmentNumber);
        if(ed==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Enrollment not found").details("enrollmentNumber="+enrollmentNumber).build());
        return ResponseEntity.ok(mapEntity(ed));
    }

    @PutMapping("/{enrollmentNumber}")
    public ResponseEntity<?> update(@PathVariable Long enrollmentNumber, @Valid @RequestBody EnrollmentDataDto dto){
        EnrollmentData existing = enrollmentService.getEnrollmentByNumber(enrollmentNumber);
        if(existing==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Enrollment not found").details("enrollmentNumber="+enrollmentNumber).build());
        try {
            StudentData student = studentService.getStudentByNumber(Long.parseLong(dto.getStudentNumber()));
            CourseDetails course = courseService.getCourseByNumber(Long.parseLong(dto.getCourseNumber()));
            if(student==null || course==null) return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid FK student or course").build());
            EnrollmentData updated = EnrollmentData.builder()
                    .enrollmentNumber(existing.getEnrollmentNumber())
                    .student(student)
                    .course(course)
                    .enrollmentDate(dto.getEnrollmentDate())
                    .overallGrade(dto.getOverallGrade())
                    .semester(dto.getSemester())
                    .instructorName(dto.getInstructorName())
                    .build();
            EnrollmentData saved = enrollmentService.createEnrollment(updated); // using save with existing number
            return ResponseEntity.ok(mapEntity(saved));
        } catch(Exception ex){
            return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid enrollment data").details(ex.getMessage()).build());
        }
    }

    @DeleteMapping("/{enrollmentNumber}")
    public ResponseEntity<?> delete(@PathVariable Long enrollmentNumber){
        EnrollmentData existing = enrollmentService.getEnrollmentByNumber(enrollmentNumber);
        if(existing==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Enrollment not found").details("enrollmentNumber="+enrollmentNumber).build());
        enrollmentService.deleteEnrollmentByNumber(enrollmentNumber); return ResponseEntity.noContent().build();
    }

    private EnrollmentDataDto mapEntity(EnrollmentData e){
        return EnrollmentDataDto.builder()
                .enrollmentNumber(e.getEnrollmentNumber()!=null?e.getEnrollmentNumber().toString():null)
                .studentNumber(e.getStudent().getStudentNumber().toString())
                .courseNumber(e.getCourse().getCourseNumber().toString())
                .enrollmentDate(e.getEnrollmentDate())
                .overallGrade(e.getOverallGrade())
                .semester(e.getSemester())
                .instructorName(e.getInstructorName())
                .build();
    }
}
