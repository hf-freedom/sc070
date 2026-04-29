package com.iot.firmware.service;

import com.iot.firmware.model.*;
import com.iot.firmware.repository.InMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UpgradeLogService {
    private final InMemoryRepository repository;
    
    public UpgradeLog createLog(String taskId, String deviceId, UpgradeTaskStatus status, String message) {
        UpgradeLog log = new UpgradeLog();
        log.setTaskId(taskId);
        log.setDeviceId(deviceId);
        log.setStatus(status);
        log.setMessage(message);
        log.setCreateTime(LocalDateTime.now());
        return repository.saveUpgradeLog(log);
    }
    
    public List<UpgradeLog> getLogsByTaskId(String taskId) {
        return repository.findLogsByTaskId(taskId);
    }
    
    public List<UpgradeLog> getLogsByDeviceId(String deviceId) {
        return repository.findLogsByDeviceId(deviceId);
    }
}
