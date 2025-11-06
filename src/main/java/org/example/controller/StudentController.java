package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.ErrorDto;
import org.example.dto.StudentAggregateCreateRequest;
import org.example.dto.StudentDataDto;
import org.example.entity.StudentData;
import org.example.entity.StudentData.Gender;
import org.example.entity.StudentData.StudentStatus;
import org.example.service.StudentDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentDataService service;

    @PostMapping
    public ResponseEntity<?> create(@Validated @RequestBody StudentDataDto dto) {
        StudentData entity = mapDtoToEntity(dto);
        StudentData saved = service.createStudent(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapEntityToDto(saved));
    }

    @PostMapping("/aggregate")
    public ResponseEntity<?> createAggregate(@Validated @RequestBody StudentAggregateCreateRequest request) {
        StudentData saved = service.createStudentAggregate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapEntityToDto(saved));
    }

    @GetMapping("/{studentNumber}")
    public ResponseEntity<?> getByNumber(@PathVariable Long studentNumber) {
        StudentData s = service.getStudentByNumber(studentNumber);
        if (s == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Student not found").details("studentNumber=" + studentNumber).build());
        return ResponseEntity.ok(mapEntityToDto(s));
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        List<StudentDataDto> students = service.getAllStudents().stream().map(this::mapEntityToDto).toList();
        return ResponseEntity.ok(students);
    }

    @PutMapping("/{studentNumber}")
    public ResponseEntity<?> update(@PathVariable Long studentNumber, @Validated @RequestBody StudentDataDto dto) {
        StudentData updated = mapDtoToEntity(dto);
        StudentData saved = service.updateStudent(studentNumber, updated);
        if (saved == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Student not found").details("studentNumber=" + studentNumber).build());
        return ResponseEntity.ok(mapEntityToDto(saved));
    }

    @PatchMapping("/{studentNumber}")
    public ResponseEntity<?> patchUpdate(@PathVariable Long studentNumber, @RequestBody Map<String, Object> updates) {
        StudentData existing = service.getStudentByNumber(studentNumber);
        if (existing == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Student not found").details("studentNumber=" + studentNumber).build());
        if (updates.containsKey("firstName")) existing.setFirstName((String) updates.get("firstName"));
        if (updates.containsKey("lastName")) existing.setLastName((String) updates.get("lastName"));
        if (updates.containsKey("dateOfBirth")) existing.setDateOfBirth(LocalDate.parse((String) updates.get("dateOfBirth")));
        if (updates.containsKey("gender")) existing.setGender(StudentData.Gender.values()[(Integer) updates.get("gender")]);
        if (updates.containsKey("joiningDate")) existing.setJoiningDate(LocalDate.parse((String) updates.get("joiningDate")));
        if (updates.containsKey("studentStatus")) existing.setStudentStatus(StudentData.StudentStatus.values()[(Integer) updates.get("studentStatus")]);
        StudentData saved = service.updateStudent(studentNumber, existing);
        return ResponseEntity.ok(mapEntityToDto(saved));
    }

    @DeleteMapping("/{studentNumber}")
    public ResponseEntity<?> delete(@PathVariable Long studentNumber) {
        StudentData existing = service.getStudentByNumber(studentNumber);
        if(existing==null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Student not found").details("studentNumber="+studentNumber).build());
        service.deleteStudent(studentNumber);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload-csv")
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty())
            return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Empty file").build());
        List<StudentDataDto> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null)
                return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Missing header row").build());
            String[] headersRaw = headerLine.split(",");
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headersRaw.length; i++) headerIndex.put(headersRaw[i].trim().toLowerCase(), i);
            String[] required = {"firstname", "lastname", "dateofbirth", "gender", "joiningdate", "studentstatus"};
            for (String r : required) if (!headerIndex.containsKey(r)) return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Missing column: " + r).build());
            String line; int row = 1;
            while ((line = reader.readLine()) != null) {
                row++; if (line.isBlank()) continue; String[] parts = line.split(",", -1);
                try {
                    StudentDataDto rowDto = StudentDataDto.builder()
                            .firstName(get(parts, headerIndex.get("firstname")))
                            .lastName(get(parts, headerIndex.get("lastname")))
                            .dateOfBirth(LocalDate.parse(get(parts, headerIndex.get("dateofbirth"))))
                            .gender(mapGender(get(parts, headerIndex.get("gender"))))
                            .joiningDate(LocalDate.parse(get(parts, headerIndex.get("joiningdate"))))
                            .studentStatus(mapStatus(get(parts, headerIndex.get("studentstatus"))))
                            .build();
                    StudentData saved = service.createStudent(mapDtoToEntity(rowDto));
                    created.add(mapEntityToDto(saved));
                } catch (Exception ex) { errors.add("Row "+row+": "+ex.getMessage()); }
            }
        }
        if (!errors.isEmpty()) return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(Map.of("inserted", created.size(), "errors", errors, "students", created));
        return ResponseEntity.ok(created);
    }

    private String get(String[] parts, int idx) { return (idx >= 0 && idx < parts.length) ? parts[idx].trim() : ""; }
    private Integer mapGender(String g) { return switch (g.toUpperCase()) { case "MALE" -> 0; case "FEMALE" -> 1; case "OTHER" -> 2; default -> 2; }; }
    private Integer mapStatus(String s) { return switch (s.toUpperCase()) { case "ACTIVE" -> 0; case "INACTIVE" -> 1; case "GRADUATED" -> 2; default -> 0; }; }

    private StudentData mapDtoToEntity(StudentDataDto dto) {
        return StudentData.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(StudentData.Gender.values()[dto.getGender()])
                .joiningDate(dto.getJoiningDate())
                .studentStatus(StudentData.StudentStatus.values()[dto.getStudentStatus()])
                .build();
    }

    private StudentDataDto mapEntityToDto(StudentData s) {
        return StudentDataDto.builder()
                .studentNumber(s.getStudentNumber()!=null? s.getStudentNumber().toString(): null)
                .firstName(s.getFirstName())
                .lastName(s.getLastName())
                .dateOfBirth(s.getDateOfBirth())
                .gender(s.getGender().ordinal())
                .joiningDate(s.getJoiningDate())
                .studentStatus(s.getStudentStatus().ordinal())
                .build();
    }
}
