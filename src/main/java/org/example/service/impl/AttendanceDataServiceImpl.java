package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.entity.AttendanceData;
import org.example.repository.AttendanceDataRepository;
import org.example.service.AttendanceDataService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceDataServiceImpl implements AttendanceDataService {
    private final AttendanceDataRepository repository;

    @Override
    public AttendanceData createAttendance(AttendanceData attendance) { return repository.save(attendance); }

    @Override
    public AttendanceData getAttendanceById(Long id) { return repository.findById(id).orElse(null); }

    @Override
    public List<AttendanceData> getAllAttendance() { return repository.findAll(); }

    @Override
    public AttendanceData updateAttendance(Long id, AttendanceData updated) {
        Optional<AttendanceData> existing = repository.findById(id);
        if (existing.isEmpty()) return null;
        updated.setAttendanceNumber(id);
        return repository.save(updated);
    }

    @Override
    public void deleteAttendance(Long id) { repository.deleteById(id); }
}
