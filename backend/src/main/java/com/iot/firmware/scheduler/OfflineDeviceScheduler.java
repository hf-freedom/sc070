package com.iot.firmware.scheduler;

import com.iot.firmware.service.UpgradeTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OfflineDeviceScheduler {
    private final UpgradeTaskService taskService;
    
    @Scheduled(fixedRateString = "${upgrade.offline-scan-interval:60000}")
    public void scanOfflineDevices() {
        taskService.retryOfflineTasks();
    }
}
