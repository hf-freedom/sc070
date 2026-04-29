package com.iot.firmware.service;

import com.iot.firmware.model.*;
import com.iot.firmware.repository.InMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpgradeTaskService {
    private final InMemoryRepository repository;
    private final DeviceService deviceService;
    private final UpgradeLogService logService;
    
    @Value("${upgrade.max-retry-count:3}")
    private Integer maxRetryCount;
    
    public Optional<UpgradeTask> getTaskById(String id) {
        return repository.findTaskById(id);
    }
    
    public List<UpgradeTask> getAllTasks() {
        return repository.findAllTasks();
    }
    
    public List<UpgradeTask> getTasksByPlanId(String planId) {
        return repository.findTasksByPlanId(planId);
    }
    
    public UpgradeTask queueTask(String taskId) {
        return repository.findTaskById(taskId)
            .map(task -> {
                Optional<Device> deviceOpt = deviceService.getDeviceById(task.getDeviceId());
                if (!deviceOpt.isPresent()) {
                    task.setStatus(UpgradeTaskStatus.FAILED);
                    task.setErrorMessage("Device not found");
                    repository.saveUpgradeTask(task);
                    return task;
                }
                
                Device device = deviceOpt.get();
                if (device.getStatus() == DeviceStatus.ONLINE) {
                    task.setStatus(UpgradeTaskStatus.DELIVERED);
                    task.setStartTime(LocalDateTime.now());
                    logService.createLog(task.getId(), task.getDeviceId(), 
                        UpgradeTaskStatus.DELIVERED, "Upgrade command delivered to device");
                    
                    simulateUpgradeProcess(task);
                } else {
                    task.setStatus(UpgradeTaskStatus.QUEUED);
                    logService.createLog(task.getId(), task.getDeviceId(), 
                        UpgradeTaskStatus.QUEUED, "Device offline, queued for later delivery");
                }
                
                return repository.saveUpgradeTask(task);
            })
            .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
    }
    
    private void simulateUpgradeProcess(UpgradeTask task) {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                updateTaskStatus(task.getId(), UpgradeTaskStatus.DOWNLOADING, "Downloading firmware");
                
                Thread.sleep(2000);
                updateTaskStatus(task.getId(), UpgradeTaskStatus.INSTALLING, "Installing firmware");
                
                Thread.sleep(2000);
                updateTaskStatus(task.getId(), UpgradeTaskStatus.REBOOTING, "Device rebooting");
                
                Thread.sleep(1000);
                
                if (Math.random() > 0.3) {
                    updateTaskStatus(task.getId(), UpgradeTaskStatus.SUCCESS, "Upgrade completed successfully");
                    
                    deviceService.getDeviceById(task.getDeviceId()).ifPresent(device -> {
                        Optional<FirmwarePackage> firmwareOpt = repository.findFirmwareById(task.getFirmwareId());
                        firmwareOpt.ifPresent(firmware -> {
                            device.setCurrentVersion(firmware.getVersion());
                            deviceService.updateDevice(device);
                        });
                    });
                } else {
                    String errorMsg = "Upgrade failed: " + (Math.random() > 0.5 ? "Download timeout" : "Installation error");
                    handleTaskFailure(task.getId(), errorMsg);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    public void updateTaskStatus(String taskId, UpgradeTaskStatus status, String message) {
        repository.findTaskById(taskId)
            .ifPresent(task -> {
                task.setStatus(status);
                task.setUpdateTime(LocalDateTime.now());
                if (status == UpgradeTaskStatus.SUCCESS || status == UpgradeTaskStatus.FAILED) {
                    task.setCompleteTime(LocalDateTime.now());
                }
                repository.saveUpgradeTask(task);
                
                logService.createLog(task.getId(), task.getDeviceId(), status, message);
            });
    }
    
    public void handleTaskFailure(String taskId, String errorMsg) {
        repository.findTaskById(taskId)
            .ifPresent(task -> {
                if (task.getRetryCount() < maxRetryCount) {
                    task.setRetryCount(task.getRetryCount() + 1);
                    task.setStatus(UpgradeTaskStatus.PENDING);
                    task.setErrorMessage(errorMsg + " (will retry)");
                    repository.saveUpgradeTask(task);
                    
                    logService.createLog(task.getId(), task.getDeviceId(), 
                        UpgradeTaskStatus.FAILED, errorMsg + " - scheduling retry " + task.getRetryCount());
                    
                    new Thread(() -> {
                        try {
                            Thread.sleep(5000);
                            queueTask(taskId);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                } else {
                    task.setStatus(UpgradeTaskStatus.FAILED);
                    task.setErrorMessage(errorMsg);
                    task.setCompleteTime(LocalDateTime.now());
                    repository.saveUpgradeTask(task);
                    
                    logService.createLog(task.getId(), task.getDeviceId(), 
                        UpgradeTaskStatus.FAILED, errorMsg + " - max retries exceeded");
                    
                    deviceService.incrementFailCount(task.getDeviceId());
                }
            });
    }
    
    public void retryOfflineTasks() {
        List<Device> offlineDevices = deviceService.getOfflineDevices();
        List<UpgradeTask> queuedTasks = repository.findQueuedTasks();
        
        for (UpgradeTask task : queuedTasks) {
            deviceService.getDeviceById(task.getDeviceId())
                .filter(d -> d.getStatus() == DeviceStatus.ONLINE)
                .ifPresent(d -> queueTask(task.getId()));
        }
    }
}
