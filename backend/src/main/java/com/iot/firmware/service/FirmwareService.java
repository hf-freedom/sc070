package com.iot.firmware.service;

import com.iot.firmware.model.FirmwarePackage;
import com.iot.firmware.model.FirmwareStatus;
import com.iot.firmware.repository.InMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FirmwareService {
    private final InMemoryRepository repository;
    
    public FirmwarePackage createFirmware(FirmwarePackage firmware) {
        firmware.setCreateTime(LocalDateTime.now());
        firmware.setUpdateTime(LocalDateTime.now());
        if (firmware.getStatus() == null) {
            firmware.setStatus(FirmwareStatus.DRAFT);
        }
        return repository.saveFirmwarePackage(firmware);
    }
    
    public Optional<FirmwarePackage> getFirmwareById(String id) {
        return repository.findFirmwareById(id);
    }
    
    public List<FirmwarePackage> getAllFirmware() {
        return repository.findAllFirmwarePackages();
    }
    
    public FirmwarePackage updateFirmware(FirmwarePackage firmware) {
        firmware.setUpdateTime(LocalDateTime.now());
        return repository.saveFirmwarePackage(firmware);
    }
    
    public FirmwarePackage publishFirmware(String id) {
        return repository.findFirmwareById(id)
            .map(firmware -> {
                firmware.setStatus(FirmwareStatus.PUBLISHED);
                firmware.setUpdateTime(LocalDateTime.now());
                return repository.saveFirmwarePackage(firmware);
            })
            .orElseThrow(() -> new RuntimeException("Firmware not found: " + id));
    }
    
    public FirmwarePackage deprecateFirmware(String id) {
        return repository.findFirmwareById(id)
            .map(firmware -> {
                firmware.setStatus(FirmwareStatus.DEPRECATED);
                firmware.setUpdateTime(LocalDateTime.now());
                return repository.saveFirmwarePackage(firmware);
            })
            .orElseThrow(() -> new RuntimeException("Firmware not found: " + id));
    }
    
    public Optional<FirmwarePackage> getLatestFirmwareForModel(String model) {
        return repository.findLatestFirmwareByModel(model);
    }
    
    public Optional<FirmwarePackage> getPreviousFirmware(String model, String currentVersion) {
        return repository.findPreviousFirmware(model, currentVersion);
    }
}
