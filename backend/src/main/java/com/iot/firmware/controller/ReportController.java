package com.iot.firmware.controller;

import com.iot.firmware.model.CoverageReport;
import com.iot.firmware.model.UpgradeLog;
import com.iot.firmware.service.ReportService;
import com.iot.firmware.service.UpgradeLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final UpgradeLogService logService;
    
    @GetMapping("/logs/task/{taskId}")
    public List<UpgradeLog> getLogsByTaskId(@PathVariable String taskId) {
        return logService.getLogsByTaskId(taskId);
    }
    
    @GetMapping("/logs/device/{deviceId}")
    public List<UpgradeLog> getLogsByDeviceId(@PathVariable String deviceId) {
        return logService.getLogsByDeviceId(deviceId);
    }
    
    @GetMapping("/reports/coverage/{firmwareId}")
    public ResponseEntity<CoverageReport> getCoverageReport(@PathVariable String firmwareId) {
        try {
            return ResponseEntity.ok(reportService.generateCoverageReport(firmwareId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
