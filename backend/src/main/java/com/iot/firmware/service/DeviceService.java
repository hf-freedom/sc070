package com.iot.firmware.service;

import com.iot.firmware.model.Device;
import com.iot.firmware.model.DeviceStatus;
import com.iot.firmware.repository.InMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final InMemoryRepository repository;
    
    public Device createDevice(Device device) {
        device.setCreateTime(LocalDateTime.now());
        device.setUpdateTime(LocalDateTime.now());
        if (device.getStatus() == null) {
            device.setStatus(DeviceStatus.ONLINE);
        }
        if (device.getFailCount() == null) {
            device.setFailCount(0);
        }
        return repository.saveDevice(device);
    }
    
    public Optional<Device> getDeviceById(String id) {
        return repository.findDeviceById(id);
    }
    
    public List<Device> getAllDevices() {
        return repository.findAllDevices();
    }
    
    public Device updateDevice(Device device) {
        device.setUpdateTime(LocalDateTime.now());
        return repository.saveDevice(device);
    }
    
    public void deleteDevice(String id) {
        repository.deleteDevice(id);
    }
    
    public Device updateDeviceStatus(String id, DeviceStatus status) {
        return repository.findDeviceById(id)
            .map(device -> {
                device.setStatus(status);
                if (status == DeviceStatus.ONLINE) {
                    device.setLastOnlineTime(LocalDateTime.now());
                }
                device.setUpdateTime(LocalDateTime.now());
                return repository.saveDevice(device);
            })
            .orElseThrow(() -> new RuntimeException("Device not found: " + id));
    }
    
    public Device incrementFailCount(String id) {
        return repository.findDeviceById(id)
            .map(device -> {
                int newCount = device.getFailCount() + 1;
                device.setFailCount(newCount);
                device.setUpdateTime(LocalDateTime.now());
                if (newCount >= 3) {
                    device.setStatus(DeviceStatus.ERROR);
                }
                return repository.saveDevice(device);
            })
            .orElseThrow(() -> new RuntimeException("Device not found: " + id));
    }
    
    public Device resetFailCount(String id) {
        return repository.findDeviceById(id)
            .map(device -> {
                device.setFailCount(0);
                device.setUpdateTime(LocalDateTime.now());
                return repository.saveDevice(device);
            })
            .orElseThrow(() -> new RuntimeException("Device not found: " + id));
    }
    
    public List<Device> getOnlineDevices() {
        return repository.findAllDevices().stream()
            .filter(d -> d.getStatus() == DeviceStatus.ONLINE)
            .collect(java.util.stream.Collectors.toList());
    }
    
    public List<Device> getOfflineDevices() {
        return repository.findAllDevices().stream()
            .filter(d -> d.getStatus() == DeviceStatus.OFFLINE)
            .collect(java.util.stream.Collectors.toList());
    }
}
