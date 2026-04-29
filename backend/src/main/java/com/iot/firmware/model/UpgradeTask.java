package com.iot.firmware.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpgradeTask {
    private String id;
    private String planId;
    private String deviceId;
    private String firmwareId;
    private String targetVersion;
    private UpgradeTaskStatus status;
    private Integer batch;
    private Integer retryCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime startTime;
    private LocalDateTime completeTime;
    private String errorMessage;
}
