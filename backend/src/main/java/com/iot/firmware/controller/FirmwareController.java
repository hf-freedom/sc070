package com.iot.firmware.controller;

import com.iot.firmware.model.FirmwarePackage;
import com.iot.firmware.service.FirmwareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/firmware")
@RequiredArgsConstructor
public class FirmwareController {
    private final FirmwareService firmwareService;
    
    @GetMapping
    public List<FirmwarePackage> getAllFirmware() {
        return firmwareService.getAllFirmware();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<FirmwarePackage> getFirmwareById(@PathVariable String id) {
        return firmwareService.getFirmwareById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public FirmwarePackage createFirmware(@RequestBody FirmwarePackage firmware) {
        return firmwareService.createFirmware(firmware);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<FirmwarePackage> updateFirmware(@PathVariable String id, @RequestBody FirmwarePackage firmware) {
        return firmwareService.getFirmwareById(id)
            .map(existing -> {
                firmware.setId(id);
                return ResponseEntity.ok(firmwareService.updateFirmware(firmware));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{id}/publish")
    public ResponseEntity<FirmwarePackage> publishFirmware(@PathVariable String id) {
        try {
            return ResponseEntity.ok(firmwareService.publishFirmware(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/deprecate")
    public ResponseEntity<FirmwarePackage> deprecateFirmware(@PathVariable String id) {
        try {
            return ResponseEntity.ok(firmwareService.deprecateFirmware(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/latest/{model}")
    public ResponseEntity<FirmwarePackage> getLatestForModel(@PathVariable String model) {
        return firmwareService.getLatestFirmwareForModel(model)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
