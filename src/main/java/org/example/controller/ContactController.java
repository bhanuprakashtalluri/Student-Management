package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.entity.ContactDetails;
import org.example.entity.StudentData;
import org.example.service.ContactDetailsService;
import org.example.service.StudentDataService;
import org.example.dto.ContactDetailsDto;
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
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {
    private final ContactDetailsService contactService;
    private final StudentDataService studentService;

    @GetMapping
    public List<ContactDetails> list(){ return contactService.getAllContacts(); }

    @PostMapping("/upload-csv")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if(file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Empty file"));
        List<ContactDetails> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))){
            String headerLine = reader.readLine();
            if(headerLine == null) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Missing header row"));
            String[] headersRaw = headerLine.split(",");
            Map<String,Integer> idx = new HashMap<>();
            for(int i=0;i<headersRaw.length;i++) idx.put(headersRaw[i].trim().toLowerCase(), i);
            String[] required = {"studentnumber","emailaddress","mobilenumber"};
            for(String r: required) if(!idx.containsKey(r)) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Missing column: "+r));
            String line; int row=1;
            while((line=reader.readLine())!=null){
                row++; if(line.isBlank()) continue; String[] parts=line.split(",", -1);
                try {
                    Long studentNumber = Long.parseLong(get(parts, idx.get("studentnumber")));
                    StudentData student = studentService.getStudentByNumber(studentNumber);
                    if(student==null) throw new IllegalArgumentException("Missing FK student");
                    ContactDetails cd = ContactDetails.builder()
                            .student(student)
                            .emailAddress(get(parts, idx.get("emailaddress")))
                            .mobileNumber(get(parts, idx.get("mobilenumber")))
                            .build();
                    created.add(contactService.createContact(cd));
                } catch(Exception ex){ errors.add("Row "+row+": "+ex.getMessage()); }
            }
        }
        if(!errors.isEmpty()) return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(Map.of("inserted",created.size(),"errors",errors,"contacts",created));
        return ResponseEntity.ok(created);
    }

    private String get(String[] parts, int i){ return (i>=0 && i<parts.length)? parts[i].trim():""; }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ContactDetailsDto dto){
        try {
            StudentData student = studentService.getStudentByNumber(Long.parseLong(dto.getStudentNumber()));
            if(student==null) return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid studentNumber").build());
            ContactDetails entity = ContactDetails.builder()
                    .student(student)
                    .emailAddress(dto.getEmailAddress())
                    .mobileNumber(dto.getMobileNumber())
                    .build();
            ContactDetails saved = contactService.createContact(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapEntity(saved));
        } catch(Exception ex){
            return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid contact data").details(ex.getMessage()).build());
        }
    }

    @GetMapping("/{contactNumber}")
    public ResponseEntity<?> get(@PathVariable Long contactNumber){
        ContactDetails cd = contactService.getContactById(contactNumber);
        if(cd==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Contact not found").details("contactNumber="+contactNumber).build());
        return ResponseEntity.ok(mapEntity(cd));
    }

    @PutMapping("/{contactNumber}")
    public ResponseEntity<?> update(@PathVariable Long contactNumber, @Valid @RequestBody ContactDetailsDto dto){
        ContactDetails existing = contactService.getContactById(contactNumber);
        if(existing==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Contact not found").details("contactNumber="+contactNumber).build());
        try {
            StudentData student = studentService.getStudentByNumber(Long.parseLong(dto.getStudentNumber()));
            if(student==null) return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid studentNumber").build());
            ContactDetails updated = ContactDetails.builder()
                    .contactNumber(contactNumber)
                    .student(student)
                    .emailAddress(dto.getEmailAddress())
                    .mobileNumber(dto.getMobileNumber())
                    .build();
            ContactDetails saved = contactService.updateContact(contactNumber, updated);
            return ResponseEntity.ok(mapEntity(saved));
        } catch(Exception ex){
            return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid contact data").details(ex.getMessage()).build());
        }
    }

    @DeleteMapping("/{contactNumber}")
    public ResponseEntity<?> delete(@PathVariable Long contactNumber){
        ContactDetails existing = contactService.getContactById(contactNumber);
        if(existing==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Contact not found").details("contactNumber="+contactNumber).build());
        contactService.deleteContact(contactNumber); return ResponseEntity.noContent().build();
    }

    private ContactDetailsDto mapEntity(ContactDetails c){
        return ContactDetailsDto.builder()
                .contactNumber(c.getContactNumber())
                .studentNumber(c.getStudent().getStudentNumber().toString())
                .emailAddress(c.getEmailAddress())
                .mobileNumber(c.getMobileNumber())
                .build();
    }
}
