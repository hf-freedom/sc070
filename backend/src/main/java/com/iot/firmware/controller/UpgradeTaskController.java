package com.iot.firmware.controller;

import com.iot.firmware.model.UpgradeTask;
import com.iot.firmware.service.UpgradeTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class UpgradeTaskController {
    private final UpgradeTaskService taskService;
    
    @GetMapping
    public List<UpgradeTask> getAllTasks() {
        return taskService.getAllTasks();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UpgradeTask> getTaskById(@PathVariable String id) {
        return taskService.getTaskById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/plan/{planId}")
    public List<UpgradeTask> getTasksByPlanId(@PathVariable String planId) {
        return taskService.getTasksByPlanId(planId);
    }
}
