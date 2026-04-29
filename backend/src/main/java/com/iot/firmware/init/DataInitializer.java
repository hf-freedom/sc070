package com.iot.firmware.init;

import com.iot.firmware.model.*;
import com.iot.firmware.repository.InMemoryRepository;
import com.iot.firmware.service.DeviceGroupService;
import com.iot.firmware.service.DeviceService;
import com.iot.firmware.service.FirmwareService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final DeviceService deviceService;
    private final DeviceGroupService groupService;
    private final FirmwareService firmwareService;
    private final InMemoryRepository repository;
    
    @Override
    public void run(String... args) {
        initDevices();
        initDeviceGroups();
        initFirmwarePackages();
    }
    
    private void initDevices() {
        List<String> models = Arrays.asList("ESP32-V1", "ESP32-V2", "STM32-L4", "STM32-F4");
        List<String> regions = Arrays.asList("CN-North", "CN-East", "CN-South", "US-East", "EU-West");
        List<String> versions = Arrays.asList("1.0.0", "1.1.0", "1.2.0");
        
        for (int i = 1; i <= 20; i++) {
            Device device = new Device();
            device.setName("Device-" + String.format("%03d", i));
            device.setModel(models.get(i % models.size()));
            device.setRegion(regions.get(i % regions.size()));
            device.setCurrentVersion(versions.get(i % versions.size()));
            device.setStatus(i % 5 == 0 ? DeviceStatus.OFFLINE : DeviceStatus.ONLINE);
            device.setFailCount(0);
            device.setLastOnlineTime(LocalDateTime.now());
            device.setCreateTime(LocalDateTime.now());
            device.setUpdateTime(LocalDateTime.now());
            deviceService.createDevice(device);
        }
    }
    
    private void initDeviceGroups() {
        List<Device> allDevices = deviceService.getAllDevices();
        
        DeviceGroup group1 = new DeviceGroup();
        group1.setName("Production-Group-1");
        group1.setDescription("生产环境设备组1");
        group1.setCreateTime(LocalDateTime.now());
        group1.setUpdateTime(LocalDateTime.now());
        group1.setDeviceIds(allDevices.subList(0, 7).stream().map(Device::getId).collect(java.util.stream.Collectors.toList()));
        groupService.createGroup(group1);
        
        DeviceGroup group2 = new DeviceGroup();
        group2.setName("Production-Group-2");
        group2.setDescription("生产环境设备组2");
        group2.setCreateTime(LocalDateTime.now());
        group2.setUpdateTime(LocalDateTime.now());
        group2.setDeviceIds(allDevices.subList(7, 14).stream().map(Device::getId).collect(java.util.stream.Collectors.toList()));
        groupService.createGroup(group2);
        
        DeviceGroup group3 = new DeviceGroup();
        group3.setName("Test-Group");
        group3.setDescription("测试环境设备组");
        group3.setCreateTime(LocalDateTime.now());
        group3.setUpdateTime(LocalDateTime.now());
        group3.setDeviceIds(allDevices.subList(14, 20).stream().map(Device::getId).collect(java.util.stream.Collectors.toList()));
        groupService.createGroup(group3);
    }
    
    private void initFirmwarePackages() {
        FirmwarePackage fw1 = new FirmwarePackage();
        fw1.setName("ESP32 Firmware");
        fw1.setVersion("1.2.0");
        fw1.setSupportedModels(Arrays.asList("ESP32-V1", "ESP32-V2"));
        fw1.setChecksum("a1b2c3d4e5f67890123456789abcdef");
        fw1.setSize(1024000);
        fw1.setDescription("ESP32系列最新稳定版本");
        fw1.setStatus(FirmwareStatus.PUBLISHED);
        fw1.setCreateTime(LocalDateTime.now());
        fw1.setUpdateTime(LocalDateTime.now());
        firmwareService.createFirmware(fw1);
        
        FirmwarePackage fw2 = new FirmwarePackage();
        fw2.setName("STM32 Firmware");
        fw2.setVersion("1.2.0");
        fw2.setSupportedModels(Arrays.asList("STM32-L4", "STM32-F4"));
        fw2.setChecksum("b2c3d4e5f67890123456789abcdef012");
        fw2.setSize(512000);
        fw2.setDescription("STM32系列最新稳定版本");
        fw2.setStatus(FirmwareStatus.PUBLISHED);
        fw2.setCreateTime(LocalDateTime.now());
        fw2.setUpdateTime(LocalDateTime.now());
        firmwareService.createFirmware(fw2);
        
        FirmwarePackage fw3 = new FirmwarePackage();
        fw3.setName("ESP32 Legacy Firmware");
        fw3.setVersion("1.1.0");
        fw3.setSupportedModels(Arrays.asList("ESP32-V1", "ESP32-V2"));
        fw3.setChecksum("c3d4e5f67890123456789abcdef01234");
        fw3.setSize(1000000);
        fw3.setDescription("ESP32系列历史稳定版本（用于回滚）");
        fw3.setStatus(FirmwareStatus.PUBLISHED);
        fw3.setCreateTime(LocalDateTime.now().minusDays(30));
        fw3.setUpdateTime(LocalDateTime.now());
        firmwareService.createFirmware(fw3);
    }
}
