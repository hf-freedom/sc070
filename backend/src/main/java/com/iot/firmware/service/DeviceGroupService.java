package com.iot.firmware.service;

import com.iot.firmware.model.DeviceGroup;
import com.iot.firmware.repository.InMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceGroupService {
    private final InMemoryRepository repository;
    
    public DeviceGroup createGroup(DeviceGroup group) {
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());
        if (group.getDeviceIds() == null) {
            group.setDeviceIds(new ArrayList<>());
        }
        return repository.saveDeviceGroup(group);
    }
    
    public Optional<DeviceGroup> getGroupById(String id) {
        return repository.findGroupById(id);
    }
    
    public List<DeviceGroup> getAllGroups() {
        return repository.findAllGroups();
    }
    
    public DeviceGroup updateGroup(DeviceGroup group) {
        group.setUpdateTime(LocalDateTime.now());
        return repository.saveDeviceGroup(group);
    }
    
    public void deleteGroup(String id) {
        repository.deleteGroup(id);
    }
    
    public DeviceGroup addDeviceToGroup(String groupId, String deviceId) {
        return repository.findGroupById(groupId)
            .map(group -> {
                if (!group.getDeviceIds().contains(deviceId)) {
                    group.getDeviceIds().add(deviceId);
                    group.setUpdateTime(LocalDateTime.now());
                    return repository.saveDeviceGroup(group);
                }
                return group;
            })
            .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
    }
    
    public DeviceGroup removeDeviceFromGroup(String groupId, String deviceId) {
        return repository.findGroupById(groupId)
            .map(group -> {
                group.getDeviceIds().remove(deviceId);
                group.setUpdateTime(LocalDateTime.now());
                return repository.saveDeviceGroup(group);
            })
            .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
    }
}
