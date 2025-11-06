package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.entity.Grades;
import org.example.entity.EnrollmentData;
import org.example.service.GradesService;
import org.example.service.EnrollmentDataService;
import org.example.dto.GradesDto;
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
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradesController {
    private final GradesService gradesService;
    private final EnrollmentDataService enrollmentService;

    @GetMapping
    public List<Grades> list(){ return gradesService.getAllGrades(); }

    @PostMapping("/upload-csv")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if(file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Empty file"));
        List<Grades> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))){
            String headerLine = reader.readLine();
            if(headerLine == null) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Missing header row"));
            String[] headersRaw = headerLine.split(",");
            Map<String,Integer> idx = new HashMap<>();
            for(int i=0;i<headersRaw.length;i++) idx.put(headersRaw[i].trim().toLowerCase(), i);
            String[] required = {"enrollmentnumber","assessmentdate","assessmenttype","obtainedscore","maxscore","gradecode"};
            for(String r: required) if(!idx.containsKey(r)) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Missing column: "+r));
            String line; int row=1;
            while((line=reader.readLine())!=null){
                row++; if(line.isBlank()) continue; String[] parts = line.split(",", -1);
                try {
                    Long enrollmentNumber = Long.parseLong(get(parts, idx.get("enrollmentnumber")));
                    EnrollmentData enrollment = enrollmentService.getEnrollmentByNumber(enrollmentNumber);
                    if(enrollment == null) throw new IllegalArgumentException("Missing FK enrollment");
                    Grades g = Grades.builder()
                            .enrollment(enrollment)
                            .assessmentDate(LocalDate.parse(get(parts, idx.get("assessmentdate"))))
                            .assessmentType(get(parts, idx.get("assessmenttype")))
                            .obtainedScore(Integer.parseInt(get(parts, idx.get("obtainedscore"))))
                            .maxScore(Integer.parseInt(get(parts, idx.get("maxscore"))))
                            .gradeCode(Integer.parseInt(get(parts, idx.get("gradecode"))))
                            .build();
                    created.add(gradesService.createGrade(g));
                } catch(Exception ex){ errors.add("Row "+row+": "+ex.getMessage()); }
            }
        }
        if(!errors.isEmpty()) return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(Map.of("inserted",created.size(),"errors",errors,"grades",created));
        return ResponseEntity.ok(created);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody GradesDto dto){
        try {
            EnrollmentData enrollment = enrollmentService.getEnrollmentByNumber(dto.getEnrollmentNumber());
            if(enrollment==null) return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid enrollmentNumber").build());
            Grades entity = Grades.builder()
                    .enrollment(enrollment)
                    .assessmentDate(dto.getAssessmentDate())
                    .assessmentType(dto.getAssessmentType())
                    .obtainedScore(dto.getObtainedScore())
                    .maxScore(dto.getMaxScore())
                    .gradeCode(dto.getGradeCode())
                    .build();
            Grades saved = gradesService.createGrade(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapEntity(saved));
        } catch(Exception ex){
            return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid grade data").details(ex.getMessage()).build());
        }
    }

    @GetMapping("/{gradeNumber}")
    public ResponseEntity<?> get(@PathVariable Long gradeNumber){
        Grades g = gradesService.getGradeById(gradeNumber);
        if(g==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Grade not found").details("gradeNumber="+gradeNumber).build());
        return ResponseEntity.ok(mapEntity(g));
    }

    @PutMapping("/{gradeNumber}")
    public ResponseEntity<?> update(@PathVariable Long gradeNumber, @Valid @RequestBody GradesDto dto){
        Grades existing = gradesService.getGradeById(gradeNumber);
        if(existing==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Grade not found").details("gradeNumber="+gradeNumber).build());
        try {
            EnrollmentData enrollment = enrollmentService.getEnrollmentByNumber(dto.getEnrollmentNumber());
            if(enrollment==null) return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid enrollmentNumber").build());
            Grades updated = Grades.builder()
                    .gradeNumber(gradeNumber)
                    .enrollment(enrollment)
                    .assessmentDate(dto.getAssessmentDate())
                    .assessmentType(dto.getAssessmentType())
                    .obtainedScore(dto.getObtainedScore())
                    .maxScore(dto.getMaxScore())
                    .gradeCode(dto.getGradeCode())
                    .build();
            Grades saved = gradesService.updateGrade(gradeNumber, updated);
            return ResponseEntity.ok(mapEntity(saved));
        } catch(Exception ex){
            return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid grade data").details(ex.getMessage()).build());
        }
    }

    @DeleteMapping("/{gradeNumber}")
    public ResponseEntity<?> delete(@PathVariable Long gradeNumber){
        Grades existing = gradesService.getGradeById(gradeNumber);
        if(existing==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Grade not found").details("gradeNumber="+gradeNumber).build());
        gradesService.deleteGrade(gradeNumber); return ResponseEntity.noContent().build();
    }

    private GradesDto mapEntity(Grades g){
        return GradesDto.builder()
                .gradeNumber(g.getGradeNumber())
                .enrollmentNumber(g.getEnrollment().getEnrollmentNumber())
                .assessmentDate(g.getAssessmentDate())
                .assessmentType(g.getAssessmentType())
                .obtainedScore(g.getObtainedScore())
                .maxScore(g.getMaxScore())
                .gradeCode(g.getGradeCode())
                .build();
    }

    private String get(String[] parts, int i){ return (i>=0 && i<parts.length)? parts[i].trim():""; }
}
