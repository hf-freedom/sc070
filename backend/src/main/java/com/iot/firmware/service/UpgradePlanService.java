package com.iot.firmware.service;

import com.iot.firmware.model.*;
import com.iot.firmware.repository.InMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpgradePlanService {
    private final InMemoryRepository repository;
    private final UpgradeTaskService taskService;
    private final DeviceService deviceService;
    private final DeviceGroupService groupService;
    
    @Value("${upgrade.success-rate-threshold:0.8}")
    private Double defaultSuccessRateThreshold;
    
    public UpgradePlan createPlan(UpgradePlan plan) {
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        plan.setStatus(UpgradePlanStatus.DRAFT);
        plan.setCurrentBatch(0);
        if (plan.getSuccessRateThreshold() == null) {
            plan.setSuccessRateThreshold(defaultSuccessRateThreshold);
        }
        return repository.saveUpgradePlan(plan);
    }
    
    public Optional<UpgradePlan> getPlanById(String id) {
        return repository.findPlanById(id);
    }
    
    public List<UpgradePlan> getAllPlans() {
        return repository.findAllPlans();
    }
    
    public UpgradePlan updatePlan(UpgradePlan plan) {
        plan.setUpdateTime(LocalDateTime.now());
        return repository.saveUpgradePlan(plan);
    }
    
    public UpgradePlan startPlan(String planId) {
        return repository.findPlanById(planId)
            .map(plan -> {
                if (plan.getStatus() != UpgradePlanStatus.DRAFT && 
                    plan.getStatus() != UpgradePlanStatus.PAUSED) {
                    throw new RuntimeException("Plan cannot be started in current status");
                }
                
                plan.setStatus(UpgradePlanStatus.RUNNING);
                plan.setStartTime(LocalDateTime.now());
                plan.setUpdateTime(LocalDateTime.now());
                repository.saveUpgradePlan(plan);
                
                createUpgradeTasks(plan);
                
                executeNextBatch(planId);
                
                return plan;
            })
            .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));
    }
    
    private void createUpgradeTasks(UpgradePlan plan) {
        java.util.Set<String> uniqueDeviceIds = new java.util.HashSet<>();
        for (String groupId : plan.getGroupIds()) {
            groupService.getGroupById(groupId)
                .ifPresent(group -> uniqueDeviceIds.addAll(group.getDeviceIds()));
        }
        
        List<String> allDeviceIds = new ArrayList<>(uniqueDeviceIds);
        int totalDevices = allDeviceIds.size();
        int grayDeviceCount = (int) (totalDevices * plan.getGrayPercentage() / 100.0);
        if (grayDeviceCount < 1 && totalDevices > 0) {
            grayDeviceCount = 1;
        }
        
        int batchSize = calculateBatchSize(grayDeviceCount);
        int totalBatches = (int) Math.ceil((double) grayDeviceCount / batchSize);
        
        plan.setTotalBatches(totalBatches);
        repository.saveUpgradePlan(plan);
        
        for (int i = 0; i < grayDeviceCount; i++) {
            String deviceId = allDeviceIds.get(i);
            int batch = (i / batchSize) + 1;
            
            UpgradeTask task = new UpgradeTask();
            task.setPlanId(plan.getId());
            task.setDeviceId(deviceId);
            task.setFirmwareId(plan.getFirmwareId());
            task.setBatch(batch);
            task.setStatus(UpgradeTaskStatus.PENDING);
            task.setRetryCount(0);
            task.setCreateTime(LocalDateTime.now());
            
            repository.saveUpgradeTask(task);
        }
    }
    
    private int calculateBatchSize(int totalDevices) {
        if (totalDevices <= 10) return Math.max(1, totalDevices);
        if (totalDevices <= 100) return 10;
        if (totalDevices <= 1000) return 50;
        return 100;
    }
    
    public void executeNextBatch(String planId) {
        repository.findPlanById(planId)
            .ifPresent(plan -> {
                if (plan.getStatus() != UpgradePlanStatus.RUNNING) {
                    return;
                }
                
                int nextBatch = plan.getCurrentBatch() + 1;
                if (nextBatch > plan.getTotalBatches()) {
                    completePlan(planId);
                    return;
                }
                
                if (nextBatch > 1 && !checkSuccessRate(planId, plan.getSuccessRateThreshold())) {
                    pausePlan(planId, "Success rate below threshold");
                    return;
                }
                
                plan.setCurrentBatch(nextBatch);
                plan.setUpdateTime(LocalDateTime.now());
                repository.saveUpgradePlan(plan);
                
                List<UpgradeTask> batchTasks = repository.findTasksByPlanId(planId).stream()
                    .filter(t -> t.getBatch() == nextBatch && t.getStatus() == UpgradeTaskStatus.PENDING)
                    .collect(java.util.stream.Collectors.toList());
                
                for (UpgradeTask task : batchTasks) {
                    taskService.queueTask(task.getId());
                }
            });
    }
    
    private boolean checkSuccessRate(String planId, double threshold) {
        List<UpgradeTask> tasks = repository.findTasksByPlanId(planId);
        long completedTasks = tasks.stream()
            .filter(t -> t.getStatus() == UpgradeTaskStatus.SUCCESS || 
                        t.getStatus() == UpgradeTaskStatus.FAILED)
            .count();
        
        if (completedTasks == 0) return true;
        
        long successTasks = tasks.stream()
            .filter(t -> t.getStatus() == UpgradeTaskStatus.SUCCESS)
            .count();
        
        double successRate = (double) successTasks / completedTasks;
        return successRate >= threshold;
    }
    
    public UpgradePlan pausePlan(String planId, String reason) {
        return repository.findPlanById(planId)
            .map(plan -> {
                plan.setStatus(UpgradePlanStatus.PAUSED);
                plan.setPauseTime(LocalDateTime.now());
                plan.setUpdateTime(LocalDateTime.now());
                return repository.saveUpgradePlan(plan);
            })
            .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));
    }
    
    public UpgradePlan completePlan(String planId) {
        return repository.findPlanById(planId)
            .map(plan -> {
                plan.setStatus(UpgradePlanStatus.COMPLETED);
                plan.setUpdateTime(LocalDateTime.now());
                return repository.saveUpgradePlan(plan);
            })
            .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));
    }
    
    public UpgradePlan cancelPlan(String planId) {
        return repository.findPlanById(planId)
            .map(plan -> {
                plan.setStatus(UpgradePlanStatus.CANCELLED);
                plan.setUpdateTime(LocalDateTime.now());
                
                List<UpgradeTask> tasks = repository.findTasksByPlanId(planId);
                for (UpgradeTask task : tasks) {
                    if (task.getStatus() == UpgradeTaskStatus.PENDING || 
                        task.getStatus() == UpgradeTaskStatus.QUEUED) {
                        task.setStatus(UpgradeTaskStatus.CANCELLED);
                        repository.saveUpgradeTask(task);
                    }
                }
                
                return repository.saveUpgradePlan(plan);
            })
            .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));
    }
}
