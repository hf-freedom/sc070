package com.iot.firmware.scheduler;

import com.iot.firmware.model.*;
import com.iot.firmware.repository.InMemoryRepository;
import com.iot.firmware.service.UpgradePlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpgradeBatchScheduler {
    private final InMemoryRepository repository;
    private final UpgradePlanService planService;
    
    @Scheduled(fixedRate = 3000)
    public void checkRunningPlans() {
        List<UpgradePlan> runningPlans = repository.findAllPlans().stream()
            .filter(p -> p.getStatus() == UpgradePlanStatus.RUNNING)
            .collect(java.util.stream.Collectors.toList());
        
        for (UpgradePlan plan : runningPlans) {
            checkAndAdvanceBatch(plan);
        }
    }
    
    private void checkAndAdvanceBatch(UpgradePlan plan) {
        List<UpgradeTask> currentBatchTasks = repository.findTasksByPlanId(plan.getId()).stream()
            .filter(t -> t.getBatch() == plan.getCurrentBatch())
            .collect(java.util.stream.Collectors.toList());
        
        if (currentBatchTasks.isEmpty()) {
            return;
        }
        
        long pendingTasks = currentBatchTasks.stream()
            .filter(t -> t.getStatus() == UpgradeTaskStatus.PENDING || 
                        t.getStatus() == UpgradeTaskStatus.QUEUED ||
                        t.getStatus() == UpgradeTaskStatus.DELIVERED ||
                        t.getStatus() == UpgradeTaskStatus.DOWNLOADING ||
                        t.getStatus() == UpgradeTaskStatus.INSTALLING ||
                        t.getStatus() == UpgradeTaskStatus.REBOOTING)
            .count();
        
        if (pendingTasks > 0) {
            log.debug("Plan {} batch {} still has {} pending tasks", 
                plan.getId(), plan.getCurrentBatch(), pendingTasks);
            return;
        }
        
        long completedTasks = currentBatchTasks.stream()
            .filter(t -> t.getStatus() == UpgradeTaskStatus.SUCCESS || 
                        t.getStatus() == UpgradeTaskStatus.FAILED)
            .count();
        
        if (completedTasks == currentBatchTasks.size() && completedTasks > 0) {
            log.info("Plan {} batch {} completed, advancing to next batch", 
                plan.getId(), plan.getCurrentBatch());
            
            int nextBatch = plan.getCurrentBatch() + 1;
            if (nextBatch <= plan.getTotalBatches()) {
                planService.executeNextBatch(plan.getId());
            } else {
                log.info("Plan {} all batches completed", plan.getId());
            }
        }
    }
}
