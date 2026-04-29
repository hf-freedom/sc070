package com.iot.firmware.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpgradeLog {
    private String id;
    private String taskId;
    private String deviceId;
    private UpgradeTaskStatus status;
    private String message;
    private LocalDateTime createTime;
}
