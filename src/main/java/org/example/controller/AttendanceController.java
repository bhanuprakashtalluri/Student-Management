package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.entity.AttendanceData;
import org.example.entity.EnrollmentData;
import org.example.entity.StudentData;
import org.example.service.AttendanceDataService;
import org.example.service.EnrollmentDataService;
import org.example.service.StudentDataService;
import org.example.dto.AttendanceDataDto;
import org.example.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceDataService attendanceService;
    private final EnrollmentDataService enrollmentService;
    private final StudentDataService studentService;

    @GetMapping
    public List<AttendanceData> list(){ return attendanceService.getAllAttendance(); }

    @PostMapping("/upload-csv")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if(file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Empty file"));
        List<AttendanceData> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))){
            String headerLine = reader.readLine();
            if(headerLine == null) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Missing header row"));
            String[] headersRaw = headerLine.split(",");
            Map<String,Integer> idx = new HashMap<>();
            for(int i=0;i<headersRaw.length;i++) idx.put(headersRaw[i].trim().toLowerCase(), i);
            String[] required = {"studentnumber","enrollmentnumber","attendancedate","attendancestatus","semester"};
            for(String r: required) if(!idx.containsKey(r)) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Missing column: "+r));
            String line; int row=1;
            while((line=reader.readLine())!=null){
                row++; if(line.isBlank()) continue; String[] parts=line.split(",", -1);
                try {
                    Long studentNumber = Long.parseLong(get(parts, idx.get("studentnumber")));
                    Long enrollmentNumber = Long.parseLong(get(parts, idx.get("enrollmentnumber")));
                    StudentData student = studentService.getStudentByNumber(studentNumber);
                    EnrollmentData enrollment = enrollmentService.getEnrollmentByNumber(enrollmentNumber);
                    if(student==null || enrollment==null) throw new IllegalArgumentException("Missing FK student or enrollment");
                    AttendanceData ad = AttendanceData.builder()
                            .student(student)
                            .enrollment(enrollment)
                            .attendanceDate(LocalDate.parse(get(parts, idx.get("attendancedate"))))
                            .attendanceStatus(AttendanceData.AttendanceStatus.valueOf(get(parts, idx.get("attendancestatus")).toUpperCase()))
                            .semester(get(parts, idx.get("semester")))
                            .build();
                    created.add(attendanceService.createAttendance(ad));
                } catch(Exception ex){ errors.add("Row "+row+": "+ex.getMessage()); }
            }
        }
        if(!errors.isEmpty()) return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(Map.of("inserted",created.size(),"errors",errors,"attendance",created));
        return ResponseEntity.ok(created);
    }

    private String get(String[] parts, int i){ return (i>=0 && i<parts.length)? parts[i].trim():""; }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody AttendanceDataDto dto){
        try {
            StudentData student = studentService.getStudentByNumber(Long.parseLong(dto.getStudentNumber()));
            EnrollmentData enrollment = enrollmentService.getEnrollmentByNumber(dto.getEnrollmentNumber());
            if(student==null || enrollment==null) return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid FK student or enrollment").build());
            AttendanceData entity = AttendanceData.builder()
                    .student(student)
                    .enrollment(enrollment)
                    .attendanceDate(dto.getAttendanceDate())
                    .attendanceStatus(AttendanceData.AttendanceStatus.values()[dto.getAttendanceStatus()])
                    .semester(dto.getSemester())
                    .build();
            AttendanceData saved = attendanceService.createAttendance(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapEntity(saved));
        } catch(Exception ex){
            return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid attendance data").details(ex.getMessage()).build());
        }
    }

    @GetMapping("/{attendanceNumber}")
    public ResponseEntity<?> get(@PathVariable Long attendanceNumber){
        AttendanceData ad = attendanceService.getAttendanceById(attendanceNumber);
        if(ad==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Attendance not found").details("attendanceNumber="+attendanceNumber).build());
        return ResponseEntity.ok(mapEntity(ad));
    }

    @PutMapping("/{attendanceNumber}")
    public ResponseEntity<?> update(@PathVariable Long attendanceNumber, @Valid @RequestBody AttendanceDataDto dto){
        AttendanceData existing = attendanceService.getAttendanceById(attendanceNumber);
        if(existing==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Attendance not found").details("attendanceNumber="+attendanceNumber).build());
        try {
            StudentData student = studentService.getStudentByNumber(Long.parseLong(dto.getStudentNumber()));
            EnrollmentData enrollment = enrollmentService.getEnrollmentByNumber(dto.getEnrollmentNumber());
            if(student==null || enrollment==null) return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid FK student or enrollment").build());
            AttendanceData updated = AttendanceData.builder()
                    .attendanceNumber(attendanceNumber)
                    .student(student)
                    .enrollment(enrollment)
                    .attendanceDate(dto.getAttendanceDate())
                    .attendanceStatus(AttendanceData.AttendanceStatus.values()[dto.getAttendanceStatus()])
                    .semester(dto.getSemester())
                    .build();
            AttendanceData saved = attendanceService.updateAttendance(attendanceNumber, updated);
            return ResponseEntity.ok(mapEntity(saved));
        } catch(Exception ex){
            return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid attendance data").details(ex.getMessage()).build());
        }
    }

    @DeleteMapping("/{attendanceNumber}")
    public ResponseEntity<?> delete(@PathVariable Long attendanceNumber){
        AttendanceData existing = attendanceService.getAttendanceById(attendanceNumber);
        if(existing==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Attendance not found").details("attendanceNumber="+attendanceNumber).build());
        attendanceService.deleteAttendance(attendanceNumber); return ResponseEntity.noContent().build();
    }

    private AttendanceDataDto mapEntity(AttendanceData a){
        return AttendanceDataDto.builder()
                .attendanceNumber(a.getAttendanceNumber())
                .studentNumber(a.getStudent().getStudentNumber().toString())
                .enrollmentNumber(a.getEnrollment().getEnrollmentNumber())
                .attendanceDate(a.getAttendanceDate())
                .attendanceStatus(a.getAttendanceStatus().ordinal())
                .semester(a.getSemester())
                .build();
    }
}
