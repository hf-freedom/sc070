package com.iot.firmware.repository;

import com.iot.firmware.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRepository {
    private final Map<String, Device> devices = new ConcurrentHashMap<>();
    private final Map<String, FirmwarePackage> firmwarePackages = new ConcurrentHashMap<>();
    private final Map<String, UpgradePlan> upgradePlans = new ConcurrentHashMap<>();
    private final Map<String, DeviceGroup> deviceGroups = new ConcurrentHashMap<>();
    private final Map<String, UpgradeTask> upgradeTasks = new ConcurrentHashMap<>();
    private final Map<String, UpgradeLog> upgradeLogs = new ConcurrentHashMap<>();
    private final Map<String, RollbackTask> rollbackTasks = new ConcurrentHashMap<>();
    
    public String generateId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    public Device saveDevice(Device device) {
        if (device.getId() == null) {
            device.setId(generateId());
        }
        devices.put(device.getId(), device);
        return device;
    }
    
    public Optional<Device> findDeviceById(String id) {
        return Optional.ofNullable(devices.get(id));
    }
    
    public List<Device> findAllDevices() {
        return new ArrayList<>(devices.values());
    }
    
    public void deleteDevice(String id) {
        devices.remove(id);
    }
    
    public FirmwarePackage saveFirmwarePackage(FirmwarePackage firmware) {
        if (firmware.getId() == null) {
            firmware.setId(generateId());
        }
        firmwarePackages.put(firmware.getId(), firmware);
        return firmware;
    }
    
    public Optional<FirmwarePackage> findFirmwareById(String id) {
        return Optional.ofNullable(firmwarePackages.get(id));
    }
    
    public List<FirmwarePackage> findAllFirmwarePackages() {
        return new ArrayList<>(firmwarePackages.values());
    }
    
    public Optional<FirmwarePackage> findLatestFirmwareByModel(String model) {
        return firmwarePackages.values().stream()
            .filter(f -> f.getSupportedModels().contains(model) && f.getStatus() == FirmwareStatus.PUBLISHED)
            .max(Comparator.comparing(FirmwarePackage::getVersion));
    }
    
    public Optional<FirmwarePackage> findPreviousFirmware(String model, String currentVersion) {
        return firmwarePackages.values().stream()
            .filter(f -> f.getSupportedModels().contains(model) 
                && f.getStatus() == FirmwareStatus.PUBLISHED
                && f.getVersion().compareTo(currentVersion) < 0)
            .max(Comparator.comparing(FirmwarePackage::getVersion));
    }
    
    public UpgradePlan saveUpgradePlan(UpgradePlan plan) {
        if (plan.getId() == null) {
            plan.setId(generateId());
        }
        upgradePlans.put(plan.getId(), plan);
        return plan;
    }
    
    public Optional<UpgradePlan> findPlanById(String id) {
        return Optional.ofNullable(upgradePlans.get(id));
    }
    
    public List<UpgradePlan> findAllPlans() {
        return new ArrayList<>(upgradePlans.values());
    }
    
    public DeviceGroup saveDeviceGroup(DeviceGroup group) {
        if (group.getId() == null) {
            group.setId(generateId());
        }
        deviceGroups.put(group.getId(), group);
        return group;
    }
    
    public Optional<DeviceGroup> findGroupById(String id) {
        return Optional.ofNullable(deviceGroups.get(id));
    }
    
    public List<DeviceGroup> findAllGroups() {
        return new ArrayList<>(deviceGroups.values());
    }
    
    public void deleteGroup(String id) {
        deviceGroups.remove(id);
    }
    
    public UpgradeTask saveUpgradeTask(UpgradeTask task) {
        if (task.getId() == null) {
            task.setId(generateId());
        }
        upgradeTasks.put(task.getId(), task);
        return task;
    }
    
    public Optional<UpgradeTask> findTaskById(String id) {
        return Optional.ofNullable(upgradeTasks.get(id));
    }
    
    public List<UpgradeTask> findAllTasks() {
        return upgradeTasks.values().stream()
            .sorted((t1, t2) -> {
                if (t1.getCompleteTime() == null && t2.getCompleteTime() == null) {
                    return t2.getCreateTime().compareTo(t1.getCreateTime());
                }
                if (t1.getCompleteTime() == null) {
                    return -1;
                }
                if (t2.getCompleteTime() == null) {
                    return 1;
                }
                return t2.getCompleteTime().compareTo(t1.getCompleteTime());
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    public List<UpgradeTask> findTasksByPlanId(String planId) {
        return upgradeTasks.values().stream()
            .filter(t -> planId.equals(t.getPlanId()))
            .sorted((t1, t2) -> {
                if (t1.getCompleteTime() == null && t2.getCompleteTime() == null) {
                    return t2.getCreateTime().compareTo(t1.getCreateTime());
                }
                if (t1.getCompleteTime() == null) {
                    return -1;
                }
                if (t2.getCompleteTime() == null) {
                    return 1;
                }
                return t2.getCompleteTime().compareTo(t1.getCompleteTime());
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    public List<UpgradeTask> findTasksByDeviceId(String deviceId) {
        return upgradeTasks.values().stream()
            .filter(t -> deviceId.equals(t.getDeviceId()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    public List<UpgradeTask> findQueuedTasks() {
        return upgradeTasks.values().stream()
            .filter(t -> t.getStatus() == UpgradeTaskStatus.QUEUED)
            .collect(java.util.stream.Collectors.toList());
    }
    
    public UpgradeLog saveUpgradeLog(UpgradeLog log) {
        if (log.getId() == null) {
            log.setId(generateId());
        }
        upgradeLogs.put(log.getId(), log);
        return log;
    }
    
    public List<UpgradeLog> findLogsByTaskId(String taskId) {
        return upgradeLogs.values().stream()
            .filter(l -> taskId.equals(l.getTaskId()))
            .sorted(Comparator.comparing(UpgradeLog::getCreateTime))
            .collect(java.util.stream.Collectors.toList());
    }
    
    public List<UpgradeLog> findLogsByDeviceId(String deviceId) {
        return upgradeLogs.values().stream()
            .filter(l -> deviceId.equals(l.getDeviceId()))
            .sorted(Comparator.comparing(UpgradeLog::getCreateTime).reversed())
            .collect(java.util.stream.Collectors.toList());
    }
    
    public RollbackTask saveRollbackTask(RollbackTask task) {
        if (task.getId() == null) {
            task.setId(generateId());
        }
        rollbackTasks.put(task.getId(), task);
        return task;
    }
    
    public Optional<RollbackTask> findRollbackById(String id) {
        return Optional.ofNullable(rollbackTasks.get(id));
    }
    
    public List<RollbackTask> findAllRollbackTasks() {
        return new ArrayList<>(rollbackTasks.values());
    }
}
