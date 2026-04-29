package com.iot.firmware.service;

import com.iot.firmware.model.*;
import com.iot.firmware.repository.InMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RollbackService {
    private final InMemoryRepository repository;
    private final DeviceService deviceService;
    private final FirmwareService firmwareService;
    private final UpgradeLogService logService;
    
    public RollbackTask createRollbackTask(String deviceId, String reason) {
        Optional<Device> deviceOpt = deviceService.getDeviceById(deviceId);
        if (!deviceOpt.isPresent()) {
            throw new RuntimeException("Device not found: " + deviceId);
        }
        
        Device device = deviceOpt.get();
        Optional<FirmwarePackage> previousFirmwareOpt = 
            firmwareService.getPreviousFirmware(device.getModel(), device.getCurrentVersion());
        
        if (!previousFirmwareOpt.isPresent()) {
            throw new RuntimeException("No previous firmware version available for rollback");
        }
        
        FirmwarePackage previousFirmware = previousFirmwareOpt.get();
        
        RollbackTask task = new RollbackTask();
        task.setDeviceId(deviceId);
        task.setFromVersion(device.getCurrentVersion());
        task.setToVersion(previousFirmware.getVersion());
        task.setFirmwareId(previousFirmware.getId());
        task.setStatus(RollbackTaskStatus.PENDING);
        task.setReason(reason);
        task.setCreateTime(LocalDateTime.now());
        
        return repository.saveRollbackTask(task);
    }
    
    public RollbackTask executeRollback(String rollbackId) {
        return repository.findRollbackById(rollbackId)
            .map(task -> {
                if (task.getStatus() != RollbackTaskStatus.PENDING) {
                    throw new RuntimeException("Rollback task cannot be executed in current status");
                }
                
                task.setStatus(RollbackTaskStatus.IN_PROGRESS);
                repository.saveRollbackTask(task);
                
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        
                        deviceService.getDeviceById(task.getDeviceId()).ifPresent(device -> {
                            Optional<FirmwarePackage> firmwareOpt = repository.findFirmwareById(task.getFirmwareId());
                            firmwareOpt.ifPresent(firmware -> {
                                device.setCurrentVersion(firmware.getVersion());
                                device.setStatus(DeviceStatus.ONLINE);
                                deviceService.updateDevice(device);
                            });
                        });
                        
                        completeRollback(rollbackId);
                    } catch (InterruptedException e) {
                        failRollback(rollbackId, "Rollback interrupted");
                        Thread.currentThread().interrupt();
                    }
                }).start();
                
                return task;
            })
            .orElseThrow(() -> new RuntimeException("Rollback task not found: " + rollbackId));
    }
    
    private void completeRollback(String rollbackId) {
        repository.findRollbackById(rollbackId)
            .ifPresent(task -> {
                task.setStatus(RollbackTaskStatus.SUCCESS);
                task.setCompleteTime(LocalDateTime.now());
                repository.saveRollbackTask(task);
            });
    }
    
    private void failRollback(String rollbackId, String errorMsg) {
        repository.findRollbackById(rollbackId)
            .ifPresent(task -> {
                task.setStatus(RollbackTaskStatus.FAILED);
                task.setErrorMessage(errorMsg);
                task.setCompleteTime(LocalDateTime.now());
                repository.saveRollbackTask(task);
            });
    }
    
    public Optional<RollbackTask> getRollbackById(String id) {
        return repository.findRollbackById(id);
    }
    
    public List<RollbackTask> getAllRollbackTasks() {
        return repository.findAllRollbackTasks();
    }
}
