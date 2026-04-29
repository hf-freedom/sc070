package com.iot.firmware.controller;

import com.iot.firmware.model.UpgradePlan;
import com.iot.firmware.service.UpgradePlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class UpgradePlanController {
    private final UpgradePlanService planService;
    
    @GetMapping
    public List<UpgradePlan> getAllPlans() {
        return planService.getAllPlans();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UpgradePlan> getPlanById(@PathVariable String id) {
        return planService.getPlanById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public UpgradePlan createPlan(@RequestBody UpgradePlan plan) {
        return planService.createPlan(plan);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UpgradePlan> updatePlan(@PathVariable String id, @RequestBody UpgradePlan plan) {
        return planService.getPlanById(id)
            .map(existing -> {
                plan.setId(id);
                return ResponseEntity.ok(planService.updatePlan(plan));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{id}/start")
    public ResponseEntity<UpgradePlan> startPlan(@PathVariable String id) {
        try {
            return ResponseEntity.ok(planService.startPlan(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/pause")
    public ResponseEntity<UpgradePlan> pausePlan(@PathVariable String id, 
                                                  @RequestParam(required = false) String reason) {
        try {
            return ResponseEntity.ok(planService.pausePlan(id, reason != null ? reason : "User paused"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<UpgradePlan> cancelPlan(@PathVariable String id) {
        try {
            return ResponseEntity.ok(planService.cancelPlan(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
