package com.iot.firmware.controller;

import com.iot.firmware.model.RollbackTask;
import com.iot.firmware.service.RollbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rollback")
@RequiredArgsConstructor
public class RollbackController {
    private final RollbackService rollbackService;
    
    @GetMapping
    public List<RollbackTask> getAllRollbackTasks() {
        return rollbackService.getAllRollbackTasks();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RollbackTask> getRollbackById(@PathVariable String id) {
        return rollbackService.getRollbackById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<RollbackTask> createRollback(@RequestParam String deviceId,
                                                        @RequestParam String reason) {
        try {
            return ResponseEntity.ok(rollbackService.createRollbackTask(deviceId, reason));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/execute")
    public ResponseEntity<RollbackTask> executeRollback(@PathVariable String id) {
        try {
            return ResponseEntity.ok(rollbackService.executeRollback(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
