package com.iot.firmware.service;

import com.iot.firmware.model.*;
import com.iot.firmware.repository.InMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final InMemoryRepository repository;
    private final DeviceService deviceService;
    
    public CoverageReport generateCoverageReport(String firmwareId) {
        Optional<FirmwarePackage> firmwareOpt = repository.findFirmwareById(firmwareId);
        if (!firmwareOpt.isPresent()) {
            throw new RuntimeException("Firmware not found: " + firmwareId);
        }
        
        FirmwarePackage firmware = firmwareOpt.get();
        String targetVersion = firmware.getVersion();
        
        List<Device> allDevices = deviceService.getAllDevices();
        List<Device> supportedDevices = allDevices.stream()
            .filter(d -> firmware.getSupportedModels().contains(d.getModel()))
            .collect(Collectors.toList());
        
        List<UpgradeTask> relatedTasks = repository.findAllTasks().stream()
            .filter(t -> firmwareId.equals(t.getFirmwareId()))
            .collect(Collectors.toList());
        
        Set<String> taskDeviceIds = relatedTasks.stream()
            .map(UpgradeTask::getDeviceId)
            .collect(Collectors.toSet());
        
        long successTaskCount = relatedTasks.stream()
            .filter(t -> t.getStatus() == UpgradeTaskStatus.SUCCESS)
            .count();
        
        long pendingCount = relatedTasks.stream()
            .filter(t -> t.getStatus() == UpgradeTaskStatus.PENDING || 
                        t.getStatus() == UpgradeTaskStatus.QUEUED ||
                        t.getStatus() == UpgradeTaskStatus.DELIVERED ||
                        t.getStatus() == UpgradeTaskStatus.DOWNLOADING ||
                        t.getStatus() == UpgradeTaskStatus.INSTALLING ||
                        t.getStatus() == UpgradeTaskStatus.REBOOTING)
            .count();
        
        long failedCount = relatedTasks.stream()
            .filter(t -> t.getStatus() == UpgradeTaskStatus.FAILED)
            .count();
        
        long alreadyUpgraded = supportedDevices.stream()
            .filter(d -> !taskDeviceIds.contains(d.getId()))
            .filter(d -> targetVersion.equals(d.getCurrentVersion()))
            .count();
        
        long totalUpgraded = successTaskCount + alreadyUpgraded;
        
        Map<String, Integer> byModel = new HashMap<>();
        Map<String, Integer> byRegion = new HashMap<>();
        
        for (Device device : supportedDevices) {
            boolean isUpgraded = false;
            
            Optional<UpgradeTask> deviceTask = relatedTasks.stream()
                .filter(t -> device.getId().equals(t.getDeviceId()))
                .findFirst();
            
            if (deviceTask.isPresent() && deviceTask.get().getStatus() == UpgradeTaskStatus.SUCCESS) {
                isUpgraded = true;
            } else if (!deviceTask.isPresent() && targetVersion.equals(device.getCurrentVersion())) {
                isUpgraded = true;
            }
            
            if (isUpgraded) {
                byModel.merge(device.getModel(), 1, Integer::sum);
                byRegion.merge(device.getRegion(), 1, Integer::sum);
            }
        }
        
        CoverageReport report = new CoverageReport();
        report.setReportId(repository.generateId());
        report.setFirmwareId(firmwareId);
        report.setVersion(targetVersion);
        report.setTotalDevices(supportedDevices.size());
        report.setUpgradedDevices((int) totalUpgraded);
        report.setPendingDevices((int) pendingCount);
        report.setFailedDevices((int) failedCount);
        report.setCoverageRate(supportedDevices.size() > 0 ? (double) totalUpgraded / supportedDevices.size() : 0.0);
        report.setByModel(byModel);
        report.setByRegion(byRegion);
        
        return report;
    }
}
