package com.iot.firmware.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RollbackTask {
    private String id;
    private String deviceId;
    private String fromVersion;
    private String toVersion;
    private String firmwareId;
    private RollbackTaskStatus status;
    private String reason;
    private LocalDateTime createTime;
    private LocalDateTime completeTime;
    private String errorMessage;
}
