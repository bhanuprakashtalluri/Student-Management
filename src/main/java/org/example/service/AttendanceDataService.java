package org.example.service;

import org.example.entity.AttendanceData;
import java.util.List;

public interface AttendanceDataService {
    AttendanceData createAttendance(AttendanceData attendance);
    AttendanceData getAttendanceById(Long id);
    List<AttendanceData> getAllAttendance();
    AttendanceData updateAttendance(Long id, AttendanceData updated);
    void deleteAttendance(Long id);
}
