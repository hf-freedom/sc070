package com.iot.firmware.controller;

import com.iot.firmware.model.DeviceGroup;
import com.iot.firmware.service.DeviceGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class DeviceGroupController {
    private final DeviceGroupService groupService;
    
    @GetMapping
    public List<DeviceGroup> getAllGroups() {
        return groupService.getAllGroups();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DeviceGroup> getGroupById(@PathVariable String id) {
        return groupService.getGroupById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public DeviceGroup createGroup(@RequestBody DeviceGroup group) {
        return groupService.createGroup(group);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DeviceGroup> updateGroup(@PathVariable String id, @RequestBody DeviceGroup group) {
        return groupService.getGroupById(id)
            .map(existing -> {
                group.setId(id);
                return ResponseEntity.ok(groupService.updateGroup(group));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id) {
        groupService.deleteGroup(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{groupId}/devices/{deviceId}")
    public ResponseEntity<DeviceGroup> addDevice(@PathVariable String groupId, @PathVariable String deviceId) {
        try {
            return ResponseEntity.ok(groupService.addDeviceToGroup(groupId, deviceId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{groupId}/devices/{deviceId}")
    public ResponseEntity<DeviceGroup> removeDevice(@PathVariable String groupId, @PathVariable String deviceId) {
        try {
            return ResponseEntity.ok(groupService.removeDeviceFromGroup(groupId, deviceId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
