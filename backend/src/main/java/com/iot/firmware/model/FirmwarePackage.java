package com.iot.firmware.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FirmwarePackage {
    private String id;
    private String name;
    private String version;
    private List<String> supportedModels;
    private String checksum;
    private Integer size;
    private String description;
    private FirmwareStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
