package com.iot.firmware.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpgradePlan {
    private String id;
    private String name;
    private String firmwareId;
    private List<String> groupIds;
    private Integer grayPercentage;
    private Integer currentBatch;
    private Integer totalBatches;
    private Double successRateThreshold;
    private UpgradePlanStatus status;
    private LocalDateTime startTime;
    private LocalDateTime pauseTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
