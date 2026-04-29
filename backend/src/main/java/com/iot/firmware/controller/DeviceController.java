package com.iot.firmware.controller;

import com.iot.firmware.model.Device;
import com.iot.firmware.model.DeviceStatus;
import com.iot.firmware.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;
    
    @GetMapping
    public List<Device> getAllDevices() {
        return deviceService.getAllDevices();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable String id) {
        return deviceService.getDeviceById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public Device createDevice(@RequestBody Device device) {
        return deviceService.createDevice(device);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable String id, @RequestBody Device device) {
        return deviceService.getDeviceById(id)
            .map(existing -> {
                device.setId(id);
                return ResponseEntity.ok(deviceService.updateDevice(device));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable String id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Device> updateStatus(@PathVariable String id, @RequestParam DeviceStatus status) {
        try {
            return ResponseEntity.ok(deviceService.updateDeviceStatus(id, status));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/online")
    public List<Device> getOnlineDevices() {
        return deviceService.getOnlineDevices();
    }
    
    @GetMapping("/offline")
    public List<Device> getOfflineDevices() {
        return deviceService.getOfflineDevices();
    }
}
