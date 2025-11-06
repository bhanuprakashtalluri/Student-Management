package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.entity.CourseDetails;
import org.example.service.CourseDetailsService;
import org.example.dto.CourseDetailsDto;
import org.example.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseDetailsService service;

    @GetMapping
    public List<CourseDetails> list() { return service.getAllCourses(); }

    @PostMapping("/upload-csv")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Empty file"));
        List<CourseDetails> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Missing header row"));
            String[] headersRaw = headerLine.split(",");
            Map<String,Integer> idx = new HashMap<>();
            for(int i=0;i<headersRaw.length;i++) idx.put(headersRaw[i].trim().toLowerCase(), i);
            String[] required = {"coursename","coursecode","coursecredits"};
            for(String r: required) if(!idx.containsKey(r)) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Missing column: "+r));
            String line; int row=1;
            while((line = reader.readLine()) != null){
                row++; if(line.isBlank()) continue; String[] parts = line.split(",", -1);
                try {
                    CourseDetails cd = CourseDetails.builder()
                            .courseName(get(parts, idx.get("coursename")))
                            .courseCode(get(parts, idx.get("coursecode")))
                            .courseCredits(Double.parseDouble(get(parts, idx.get("coursecredits"))))
                            .build();
                    created.add(service.createCourse(cd));
                } catch(Exception ex){ errors.add("Row "+row+": "+ex.getMessage()); }
            }
        }
        if(!errors.isEmpty()) return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(Map.of("inserted",created.size(),"errors",errors,"courses",created));
        return ResponseEntity.ok(created);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CourseDetailsDto dto) {
        try {
            CourseDetails entity = mapDtoToEntity(dto);
            CourseDetails saved = service.createCourse(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapEntityToDto(saved));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid course data").details(ex.getMessage()).build());
        }
    }

    @GetMapping("/{courseNumber}")
    public ResponseEntity<?> getByNumber(@PathVariable Long courseNumber) {
        CourseDetails course = service.getCourseByNumber(courseNumber);
        if(course==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Course not found").details("courseNumber="+courseNumber).build());
        return ResponseEntity.ok(mapEntityToDto(course));
    }

    @PutMapping("/{courseNumber}")
    public ResponseEntity<?> update(@PathVariable Long courseNumber, @Valid @RequestBody CourseDetailsDto dto) {
        try {
            CourseDetails updated = mapDtoToEntity(dto);
            CourseDetails saved = service.updateCourse(courseNumber, updated);
            if(saved==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Course not found").details("courseNumber="+courseNumber).build());
            return ResponseEntity.ok(mapEntityToDto(saved));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid course data").details(ex.getMessage()).build());
        }
    }

    @DeleteMapping("/{courseNumber}")
    public ResponseEntity<?> delete(@PathVariable Long courseNumber) {
        CourseDetails existing = service.getCourseByNumber(courseNumber);
        if(existing==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Course not found").details("courseNumber="+courseNumber).build());
        service.deleteCourse(courseNumber);
        return ResponseEntity.noContent().build();
    }

    private CourseDetailsDto mapEntityToDto(CourseDetails c) {
        return CourseDetailsDto.builder()
                .courseNumber(c.getCourseNumber()!=null?c.getCourseNumber().toString():null)
                .courseName(c.getCourseName())
                .courseCode(c.getCourseCode())
                .courseCredits(c.getCourseCredits())
                .build();
    }
    private CourseDetails mapDtoToEntity(CourseDetailsDto dto) {
        return CourseDetails.builder()
                .courseName(dto.getCourseName())
                .courseCode(dto.getCourseCode())
                .courseCredits(dto.getCourseCredits())
                .build();
    }
    private String get(String[] parts, int i){ return (i>=0 && i<parts.length)? parts[i].trim():""; }
}
