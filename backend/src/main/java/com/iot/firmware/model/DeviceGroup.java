package com.iot.firmware.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DeviceGroup {
    private String id;
    private String name;
    private String description;
    private List<String> deviceIds;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
