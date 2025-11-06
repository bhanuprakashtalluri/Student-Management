package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.entity.AddressDetails;
import org.example.entity.StudentData;
import org.example.service.AddressDetailsService;
import org.example.service.StudentDataService;
import org.example.dto.AddressDetailsDto;
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
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressDetailsService addressService;
    private final StudentDataService studentService;

    @GetMapping
    public List<AddressDetails> list(){ return addressService.getAllAddresses(); }

    @PostMapping("/upload-csv")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if(file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Empty file"));
        List<AddressDetails> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))){
            String headerLine = reader.readLine();
            if(headerLine == null) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Missing header row"));
            String[] headersRaw = headerLine.split(",");
            Map<String,Integer> idx = new HashMap<>();
            for(int i=0;i<headersRaw.length;i++) idx.put(headersRaw[i].trim().toLowerCase(), i);
            String[] required = {"studentnumber","street","city","state","zipcode"};
            for(String r: required) if(!idx.containsKey(r)) return ResponseEntity.badRequest().body(Map.of("status",400,"message","Missing column: "+r));
            String line; int row=1;
            while((line=reader.readLine())!=null){
                row++; if(line.isBlank()) continue; String[] parts=line.split(",", -1);
                try {
                    Long studentNumber = Long.parseLong(get(parts, idx.get("studentnumber")));
                    StudentData student = studentService.getStudentByNumber(studentNumber);
                    if(student==null) throw new IllegalArgumentException("Missing FK student");
                    AddressDetails ad = AddressDetails.builder()
                            .student(student)
                            .street(get(parts, idx.get("street")))
                            .city(get(parts, idx.get("city")))
                            .state(get(parts, idx.get("state")))
                            .zipCode(get(parts, idx.get("zipcode")))
                            .build();
                    created.add(addressService.createAddress(ad));
                } catch(Exception ex){ errors.add("Row "+row+": "+ex.getMessage()); }
            }
        }
        if(!errors.isEmpty()) return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(Map.of("inserted",created.size(),"errors",errors,"addresses",created));
        return ResponseEntity.ok(created);
    }

    private String get(String[] parts, int i){ return (i>=0 && i<parts.length)? parts[i].trim():""; }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody AddressDetailsDto dto){
        try {
            StudentData student = studentService.getStudentByNumber(Long.parseLong(dto.getStudentNumber()));
            if(student==null) return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid studentNumber").build());
            AddressDetails entity = AddressDetails.builder()
                    .student(student)
                    .street(dto.getStreet())
                    .city(dto.getCity())
                    .state(dto.getState())
                    .zipCode(dto.getZipCode())
                    .build();
            AddressDetails saved = addressService.createAddress(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapEntity(saved));
        } catch(Exception ex){
            return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid address data").details(ex.getMessage()).build());
        }
    }

    @GetMapping("/{addressNumber}")
    public ResponseEntity<?> get(@PathVariable Long addressNumber){
        AddressDetails ad = addressService.getAddressById(addressNumber);
        if(ad==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Address not found").details("addressNumber="+addressNumber).build());
        return ResponseEntity.ok(mapEntity(ad));
    }

    @PutMapping("/{addressNumber}")
    public ResponseEntity<?> update(@PathVariable Long addressNumber, @Valid @RequestBody AddressDetailsDto dto){
        AddressDetails existing = addressService.getAddressById(addressNumber);
        if(existing==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Address not found").details("addressNumber="+addressNumber).build());
        try {
            StudentData student = studentService.getStudentByNumber(Long.parseLong(dto.getStudentNumber()));
            if(student==null) return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid studentNumber").build());
            AddressDetails updated = AddressDetails.builder()
                    .addressNumber(addressNumber)
                    .student(student)
                    .street(dto.getStreet())
                    .city(dto.getCity())
                    .state(dto.getState())
                    .zipCode(dto.getZipCode())
                    .build();
            AddressDetails saved = addressService.updateAddress(addressNumber, updated);
            return ResponseEntity.ok(mapEntity(saved));
        } catch(Exception ex){
            return ResponseEntity.badRequest().body(ErrorDto.builder().status(400).message("Invalid address data").details(ex.getMessage()).build());
        }
    }

    @DeleteMapping("/{addressNumber}")
    public ResponseEntity<?> delete(@PathVariable Long addressNumber){
        AddressDetails existing = addressService.getAddressById(addressNumber);
        if(existing==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDto.builder().status(404).message("Address not found").details("addressNumber="+addressNumber).build());
        addressService.deleteAddress(addressNumber);
        return ResponseEntity.noContent().build();
    }

    private AddressDetailsDto mapEntity(AddressDetails a){
        return AddressDetailsDto.builder()
                .addressNumber(a.getAddressNumber())
                .studentNumber(a.getStudent().getStudentNumber().toString())
                .street(a.getStreet())
                .city(a.getCity())
                .state(a.getState())
                .zipCode(a.getZipCode())
                .build();
    }
}
