package com.iot.firmware.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Device {
    private String id;
    private String name;
    private String model;
    private String region;
    private String currentVersion;
    private DeviceStatus status;
    private String groupId;
    private Integer failCount;
    private LocalDateTime lastOnlineTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
